package com.lhl.common.core.helper

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonInclude
import io.github.oshai.kotlinlogging.KotlinLogging
import tools.jackson.core.JsonGenerator
import tools.jackson.core.json.JsonReadFeature
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.node.ObjectNode
import tools.jackson.databind.ser.PropertyWriter
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter
import tools.jackson.databind.ser.std.SimpleFilterProvider
import tools.jackson.databind.ser.std.ToStringSerializer
import tools.jackson.databind.util.StdDateFormat
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule
import java.io.InputStream
import java.io.Serializable
import java.time.ZoneId
import java.util.*

val log = KotlinLogging.logger {}

/**
 * Jackson 3.x 优化版工具类
 *
 * 主要功能：
 * - JSON 序列化/反序列化
 * - 动态字段过滤
 * - Kotlin、Java 8、JavaTime 支持（已内置）
 * - 类型转换扩展函数
 */
object JacksonHelper : Serializable {

    private const val serialVersionUID = 1L
    private const val DYNAMIC_FILTER_ID = "DYNAMIC_FILTER"

    /**
     * 全局 ObjectMapper 实例
     * 配置了常用的序列化/反序列化特性
     *
     * 注意: Jackson 3.x 已将 Jdk8Module 和 JavaTimeModule 内置到核心库中
     */
    @JvmStatic
    val mapper: ObjectMapper = JsonMapper.builder()
        // 1. 解决前端 Long 精度丢失问题：将 Long/long 统一序列化为 String
        .addModule(
            SimpleModule("SmartLongModule").apply {
                addSerializer(Long::class.java, SmartLongSerializer())
                addSerializer(Long::class.javaPrimitiveType, SmartLongSerializer())
            }
        )
        // 2. 序列化时忽略值为 null 的字段
        .changeDefaultPropertyInclusion { _ ->
            JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS)
        }
        // 3.日期配置
        // 设置默认时区为系统时区
        .defaultTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
        // 设置默认地区为中国
        .defaultLocale(Locale.CHINA)
        // 默认日期格式
        .defaultDateFormat(StdDateFormat().withColonInTimeZone(true))
        // 4. 只保留源码中存在的枚举项
        // 禁用空 Bean 序列化失败特性
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        // 禁用未知属性反序列化失败特性
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        // 禁用基本类型 null 值失败特性
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        // 5. Stream 级别特性 (检查你的导入路径)
        // 允许未转义控制字符
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        // 允许反斜杠转义任意字符
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        // 允许单引号
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        // 允许不带引号的字段名
        .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
        // 允许 Java 注释
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        // 添加 Kotlin 支持模块
        .addModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                // 注意: Jackson 3.x Kotlin Module 的特性配置
                // 可根据需要启用或禁用以下特性:
                 .enable(KotlinFeature.StrictNullChecks) // 启用严格的 null 检查
                 .enable(KotlinFeature.KotlinPropertyNameAsImplicitName) // 使用 Kotlin 属性名
                 .enable(KotlinFeature.SingletonSupport) // 支持单例对象
                 .disable(KotlinFeature.NullToEmptyCollection) // 不将 null 转换为空集合
                 .disable(KotlinFeature.NullToEmptyMap) // 不将 null 转换为空 Map
                 .disable(KotlinFeature.NullIsSameAsDefault) // null 不等同于默认值
                .build()
        )
        // 自动注册其他已发现的模块
        .findAndAddModules()
        .build()

    // ==================== 动态字段过滤 ====================

    @JsonFilter(DYNAMIC_FILTER_ID)
    interface DynamicFilter

    /**
     * 创建只包含指定字段的 Writer（支持嵌套字段）
     *
     * 示例：
     * ```kotlin
     * val writer = writerWithFields(User::class.java, "id", "name", "address.city")
     * val json = writer.writeValueAsString(user)
     * ```
     *
     * @param clazz 目标类
     * @param fields 要包含的字段列表，支持嵌套字段如 "user.address.street"
     * @return ObjectWriter 对象
     */
    @JvmStatic
    fun writerWithFields(clazz: Class<*>, vararg fields: String): ObjectWriter {
        if (fields.isEmpty()) return mapper.writer()

        val fieldSet = fields.toSet()
        val filter = object : SimpleBeanPropertyFilter() {
            override fun serializeAsProperty(
                bean: Any,
                gen: JsonGenerator,
                ctxt: SerializationContext,
                writer: PropertyWriter
            ) {
                val name = writer.name
                // 字段名完全匹配或者是嵌套字段的前缀
                if (fieldSet.contains(name) || fieldSet.any { it.startsWith("$name.") }) {
                    writer.serializeAsProperty(bean, gen, ctxt)
                }
                // 否则跳过该字段
            }
        }

        val filterProvider = SimpleFilterProvider()
            .addFilter(DYNAMIC_FILTER_ID, filter)
            .setFailOnUnknownId(false) // 不因未知过滤器 ID 而失败

        // Jackson 3.x: 使用 rebuild 创建新的 mapper 实例
        return (mapper as JsonMapper).rebuild()
            .addMixIn(clazz, DynamicFilter::class.java)
            .build()
            .writer(filterProvider)
    }

    /**
     * 创建排除指定字段的 Writer
     *
     * @param clazz 目标类
     * @param fields 要排除的字段列表
     * @return ObjectWriter 对象
     */
    @JvmStatic
    fun writerWithoutFields(clazz: Class<*>, vararg fields: String): ObjectWriter {
        if (fields.isEmpty()) return mapper.writer()

        val fieldSet = fields.toSet()
        val filter = object : SimpleBeanPropertyFilter() {
            override fun serializeAsProperty(
                bean: Any,
                gen: JsonGenerator,
                ctxt: SerializationContext,
                writer: PropertyWriter
            ) {
                val name = writer.name
                // 字段名不在排除列表中
                if (!fieldSet.contains(name)) {
                    writer.serializeAsProperty(bean, gen, ctxt)
                }
            }
        }

        val filterProvider = SimpleFilterProvider()
            .addFilter(DYNAMIC_FILTER_ID, filter)
            .setFailOnUnknownId(false)

        return (mapper as JsonMapper).rebuild()
            .addMixIn(clazz, DynamicFilter::class.java)
            .build()
            .writer(filterProvider)
    }

    // ==================== 便捷方法 ====================

    /**
     * 将对象序列化为 JSON 字符串
     * @param value 待序列化的对象
     * @return JSON 字符串
     */
    @JvmStatic
    fun writeValueAsString(value: Any?): String = mapper.writeValueAsString(value)

    /**
     * 将对象序列化为格式化的 JSON 字符串
     * @param value 待序列化的对象
     * @return 格式化的 JSON 字符串
     */
    @JvmStatic
    fun writeValueAsPrettyString(value: Any?): String =
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     * @param content JSON 字符串
     * @param clazz 目标类型
     * @return 指定类型的对象
     */
    @JvmStatic
    fun <T> readValue(content: String, clazz: Class<T>): T = mapper.readValue(content, clazz)

    /**
     * 将 JSON 字符串反序列化为指定类型的对象（Kotlin inline 版本）
     * @param content JSON 字符串
     * @return 指定类型的对象
     */
    @JvmStatic
    inline fun <reified T> readValue(content: String): T = mapper.readValue(content, T::class.java)

    /**
     * 将 JSON 字符串反序列化为指定类型的对象（使用 TypeReference）
     * @param content JSON 字符串
     * @param valueType TypeReference 对象
     * @return 指定类型的对象
     */
    @JvmStatic
    fun <T> readValue(content: String, valueType: TypeReference<T>): T =
        mapper.readValue(content, valueType)

    /**
     * 对象类型转换
     * @param value 源对象
     * @param toValueType 目标类型
     * @return 转换后的对象
     */
    @JvmStatic
    fun <T> convertValue(value: Any?, toValueType: Class<T>): T =
        mapper.convertValue(value, toValueType)

    /**
     * 对象类型转换（使用 TypeReference）
     * @param value 源对象
     * @param toValueType TypeReference 对象
     * @return 转换后的对象
     */
    @JvmStatic
    fun <T> convertValue(value: Any?, toValueType: TypeReference<T>): T =
        mapper.convertValue(value, toValueType)
}

// ==================================== Kotlin 扩展函数 ====================================

/**
 * 将当前对象转换为目标类型 T
 *
 * 支持多种转换场景：
 * 1. null 值直接返回 null
 * 2. String 类型尝试解析为 JSON 对象
 * 3. JsonNode 类型尝试转换为目标对象
 * 4. 其他类型尝试直接转换
 *
 * @param T 目标类型
 * @return 转换后的对象，失败时返回 null
 */
inline fun <reified T> Any?.toType(): T? = when {
    this == null -> null

    this is String && this.isNotBlank() -> runCatching {
        JacksonHelper.mapper.readValue(this, T::class.java)
    }.onFailure {
        log.error(it) { "JSON 字符串转换失败: String → ${T::class.java.simpleName}" }
    }.getOrNull()

    this is JsonNode -> runCatching {
        JacksonHelper.mapper.treeToValue(this, T::class.java)
    }.onFailure {
        log.error(it) { "JsonNode 转换失败: JsonNode → ${T::class.java.simpleName}" }
    }.getOrNull()

    else -> runCatching {
        JacksonHelper.mapper.convertValue(this, T::class.java)
    }.onFailure {
        log.error(it) { "对象转换失败: ${this::class.java.simpleName} → ${T::class.java.simpleName}" }
    }.getOrNull()
}

/**
 * 将当前对象转换为指定类型的对象（使用 TypeReference）
 *
 * @param T 目标类型
 * @param typeRef TypeReference 对象，用于复杂泛型类型转换
 * @return 转换后的对象，失败时返回 null
 */
fun <T> Any?.toType(typeRef: TypeReference<T>): T? = when {
    this == null -> null

    this is String && this.isNotBlank() -> runCatching {
        JacksonHelper.mapper.readValue(this, typeRef)
    }.onFailure {
        log.error(it) { "JSON 字符串转换失败: String → ${typeRef.type}" }
    }.getOrNull()

    this is JsonNode -> runCatching {
        JacksonHelper.mapper.treeToValue(this, typeRef)
    }.onFailure {
        log.error(it) { "JsonNode 转换失败: JsonNode → ${typeRef.type}" }
    }.getOrNull()

    else -> runCatching {
        JacksonHelper.mapper.convertValue(this, typeRef)
    }.onFailure {
        log.error(it) { "对象转换失败: ${this?.javaClass?.simpleName} → ${typeRef.type}" }
    }.getOrNull()
}

/**
 * 将 InputStream 转换为 JsonNode
 *
 * @return JsonNode 对象，失败时返回 null
 */
fun InputStream.toJsonNode(): JsonNode? = runCatching {
    JacksonHelper.mapper.readTree(this)
}.onFailure {
    log.error(it) { "InputStream → JsonNode 转换失败" }
}.getOrNull()

/**
 * 将当前对象序列化为 JSON 字符串
 *
 * @param pretty 是否格式化输出，默认为 false
 * @return JSON 字符串，失败时返回 null
 */
fun Any?.toJsonString(pretty: Boolean = false): String? = when {
    this == null -> null

    pretty -> runCatching {
        JacksonHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }.onFailure {
        log.error(it) { "对象序列化失败（格式化）: ${this::class.java.simpleName} → JSON" }
    }.getOrNull()

    else -> runCatching {
        JacksonHelper.mapper.writeValueAsString(this)
    }.onFailure {
        log.error(it) { "对象序列化失败: ${this::class.java.simpleName} → JSON" }
    }.getOrNull()
}

/**
 * 将当前对象序列化为格式化的 JSON 字符串
 *
 * @return 格式化的 JSON 字符串，失败时返回 null
 */
fun Any?.toPrettyJsonString(): String? = toJsonString(pretty = true)

/**
 * 将 JSON 字符串解析为 JsonNode
 *
 * @return JsonNode 对象，失败时返回 null
 */
fun String.toJsonNode(): JsonNode? = takeIf { it.isNotBlank() }?.let {
    runCatching {
        JacksonHelper.mapper.readTree(it)
    }.onFailure { ex ->
        log.error(ex) { "JSON 字符串解析失败: String → JsonNode" }
    }.getOrNull()
}

/**
 * 将 JSON 字符串解析为 ObjectNode
 *
 * @return ObjectNode 对象，失败时返回 null
 */
fun String.toObjectNode(): ObjectNode? = toJsonNode() as? ObjectNode

/**
 * 将 JSON 字符串解析为 Map<String, Any?>
 *
 * @return Map 对象，或在失败/空输入时返回 null
 */
fun String.toMap(): Map<String, Any?>? = toType()

/**
 * 将 JSON 字符串转换为指定类型的 List
 *
 * @param T List 元素类型
 * @return 指定类型的 List，失败时返回 null
 */
inline fun <reified T> String.toList(): List<T>? = toType()

/**
 * 将 JSON 字符串转换为指定类型的集合
 *
 * @param T 集合元素类型
 * @return 指定类型的集合，失败时返回 null
 */
inline fun <reified T> String.toCollection(): Collection<T>? = toType()

/**
 * 将 Map 转换为指定的数据类对象
 *
 * @param T 数据类类型
 * @return 数据类实例，失败时返回 null
 */
inline fun <reified T> Map<String, Any?>.toDataClass(): T? = runCatching {
    JacksonHelper.mapper.convertValue(this, T::class.java)
}.onFailure {
    log.error(it) { "Map 转换失败: Map → ${T::class.java.simpleName}" }
}.getOrNull()

/**
 * 将当前对象转换为 ObjectNode
 *
 * @return ObjectNode 对象，失败时返回 null
 */
fun Any?.toObjectNode(): ObjectNode? = toType()

/**
 * 将当前对象转换为 Map
 *
 * @return Map<String, Any?> 对象，失败时返回 null
 */
fun Any?.toMap(): Map<String, Any?>? = toType()

/**
 * 深拷贝对象
 *
 * @param T 对象类型
 * @return 深拷贝后的对象，失败时返回 null
 */
inline fun <reified T> T.deepCopy(): T? = runCatching {
    val json = JacksonHelper.mapper.writeValueAsString(this)
    JacksonHelper.mapper.readValue(json, T::class.java)
}.onFailure {
    log.error(it) { "对象深拷贝失败: ${T::class.java.simpleName}" }
}.getOrNull()