package com.lhl.common.core.helper

import cn.hutool.v7.core.date.DateTime
import cn.hutool.v7.core.date.DateUtil
import cn.hutool.v7.json.JSONUtil
import org.springframework.core.ParameterizedTypeReference
import tools.jackson.core.type.TypeReference
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


/**
 * 函数式接口，用于将 From 类型映射到 To 类型
 *
 * @param From 输入类型
 * @param To 输出类型
 */
fun interface Mapper<in From, out To> {
    /**
     * 将单个 From 对象映射为 To 对象
     *
     * @param from 输入对象
     * @return 转换后的对象
     */
    fun map(from: From): To
}

/**
 * 将 Mapper 应用于列表中的每个元素，将 List<From> 转换为 List<To>
 *
 * @param list 输入列表，元素类型为 From
 * @return 转换后的列表，元素类型为 To
 */
fun <F, T> Mapper<F, T>.mapAll(list: List<F>): List<T> = list.map { map(it) }

/**
 * 定义在 List 集合上。它的作用是将一个 List<In> 转换为另一个类型的 List<Out>，
 * 类似于 Kotlin 的 map 函数，但它使用了 Java 的 Stream API 来实现
 *
 * @param In
 * @param Out
 * @param mapper
 * @return
 */
fun <In, Out> List<In>.mapToList(mapper: Function<In, Out>): List<Out> {
    return stream().map(mapper).collect(Collectors.toList())
}

// kudos to https://gist.github.com/maiconhellmann/796debb4007139d50e39f139be08811c
fun Date.add(field: Int, amount: Int): Date {
    Calendar.getInstance().apply {
        time = this@add
        add(field, amount)
        return time
    }
}

fun LocalDate?.toDate(): Date? = when {
    (this != null) -> try {
        Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
    } catch (e: Exception) {
        null
    }

    else -> null
}

fun LocalDateTime?.toDate(): Date? = when {
    (this != null) -> try {
        Date.from(atZone(ZoneId.systemDefault()).toInstant())
    } catch (e: Exception) {
        null
    }

    else -> null
}

// Optional 转换为 Kotlin 的可空类型
fun <T> Optional<T>.toNullable(): T? = this.orElse(null)

// 添加一个泛型扩展函数,用来判断数据是否为空字符串
fun <R> String.notBlank(block: String.() -> R): R? {
    if (this.isNotBlank()) {
        return block()
    }
    return null
}

// 扩展函数：将Long转换为DateTime
fun Long.parseDateTime(): DateTime {
    return DateUtil.date(this)
}

// 扩展函数：将Long转换为LocalDateTime
fun Long.parseLocalDateTime(): LocalDateTime {
    return DateUtil.toLocalDateTime(DateUtil.date(this))
}

// 扩展函数：将String转换为LocalDate
fun String.parseLocalDate(): LocalDate {
    return LocalDate.parse(this)
}

/**
 * 根据条件应用代码块
 * val number = 5
 * val formattedNumber = number.applyIf(number > 10) {
 *     toFormattedString()
 * }
 */
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    return if (condition) {
        this.apply(block)
    } else {
        this
    }
}

/**
 * 通过扩展函数,对象转成格式化json字符串
 */
inline fun <reified T : Any> T.toHutoolJsonPretty(): String = JSONUtil.toJsonPrettyStr(this)

/**
 * 通过扩展函数,对象转成json字符串
 *
 * @param T
 * @return
 */
inline fun <reified T : Any> T.toHutoolJson(): String = JSONUtil.toJsonStr(this)

/**
 * 这是个扩展函数，给 ParameterizedTypeReference<T> 添加了一个方法：toJacksonTypeRef()
 * 作用：把 Spring 的泛型类型参考 转换成 Jackson 能识别的 TypeReference
 */
inline fun <reified T : Any> typeReference() = object : ParameterizedTypeReference<T>() {}
inline fun <reified T : Any> ParameterizedTypeReference<T>.toJacksonTypeRef(): TypeReference<T> {
    val theType: Type = this.type
    return object : TypeReference<T>() {
        override fun getType(): Type = theType
    }
}
/**

// 假设你要调用接口返回 List<User>
val response = restTemplate.exchange(
url,
HttpMethod.GET,
null,
typeReference<List<User>>()   // 这里用了第一个工具函数
)

// 拿到响应后，想用 ObjectMapper 反序列化一个复杂的泛型对象
val jacksonType = typeReference<Map<String, List<User>>>()
.toJacksonTypeRef()   // 转换成 Jackson 能用的

val result: Map<String, List<User>> = objectMapper.readValue(jsonString, jacksonType)

 */





