package com.lhl.common.core.helper

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import kotlin.jvm.java

/**
 * 验证工具类 - 提供快速验证和完整验证能力
 *
 * 设计原则：
 * 1. 快速验证（failFast=true）：性能优先，适用于值对象和参数校验
 * 2. 完整验证（failFast=false）：完整性优先，适用于表单验证等场景
 * 3. 统一的异常处理和结果封装
 */
object ValidationHelper {

    // 快速失败模式的 Validator
    private val fastValidator: Validator by lazy {
        createValidator(failFast = true)
    }

    // 完整验证模式的 Validator
    private val completeValidator: Validator by lazy {
        createValidator(failFast = false)
    }

    private fun createValidator(failFast: Boolean): Validator {
        return Validation.byProvider(HibernateValidator::class.java)
            .configure()
            .failFast(failFast)
            .buildValidatorFactory()
            .validator
    }

    /**
     * 快速验证 - 发现第一个错误就停止
     * @param obj 要验证的对象
     * @return 验证结果，包含是否成功和错误信息
     */
    fun <T> validateQuick(obj: T): ValidationResult {
        val violations = fastValidator.validate(obj)
        return if (violations.isEmpty()) {
            ValidationResult.success()
        } else {
            val violation = violations.first()
            val error = ValidationError(violation.propertyPath.toString(), violation.message)
            ValidationResult.failure(listOf(error))
        }
    }

    /**
     * 完整验证 - 收集所有验证错误
     * @param obj 要验证的对象
     * @return 验证结果，包含所有错误信息
     */
    fun <T> validateComplete(obj: T): ValidationResult {
        val violations = completeValidator.validate(obj)
        return if (violations.isEmpty()) {
            ValidationResult.success()
        } else {
            val errors = violations.map { violation ->
                ValidationError(violation.propertyPath.toString(), violation.message)
            }
            ValidationResult.failure(errors)
        }
    }

    /**
     * 快速验证并抛出异常
     * @param obj 要验证的对象
     * @param exceptionProvider 自定义异常提供者，默认使用 IllegalArgumentException
     * @throws Exception 验证失败时抛出异常
     */
    fun <T> validateQuickOrThrow(
        obj: T,
        exceptionProvider: (String) -> Exception = ::IllegalArgumentException
    ) {
        val result = validateQuick(obj)
        if (!result.isValid) {
            throw exceptionProvider(result.getFirstErrorMessage())
        }
    }

    /**
     * 完整验证并抛出异常
     * @param obj 要验证的对象
     * @param exceptionProvider 自定义异常提供者，默认使用 IllegalArgumentException
     * @throws Exception 验证失败时抛出异常
     */
    fun <T> validateCompleteOrThrow(
        obj: T,
        exceptionProvider: (String) -> Exception = ::IllegalArgumentException
    ) {
        val result = validateComplete(obj)
        if (!result.isValid) {
            val errorMessage = result.getAllErrorMessages().joinToString("; ")
            throw exceptionProvider(errorMessage)
        }
    }
}

/**
 * 验证结果封装类
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun success() = ValidationResult(true)
        fun failure(errors: List<ValidationError>) = ValidationResult(false, errors)
    }

    /**
     * 如果验证失败则抛出异常
     */
    fun throwIfInvalid(exceptionProvider: (String) -> Exception = ::IllegalArgumentException) {
        if (!isValid) {
            val errorMessage = getAllErrorMessages().joinToString("; ")
            throw exceptionProvider(errorMessage)
        }
    }

    /**
     * 获取第一个错误
     */
    fun getFirstError(): ValidationError? = errors.firstOrNull()

    /**
     * 获取第一个错误消息
     */
    fun getFirstErrorMessage(): String =
        getFirstError()?.let { "${it.field}: ${it.message}" } ?: ""

    /**
     * 获取所有错误消息
     */
    fun getAllErrorMessages(): List<String> =
        errors.map { "${it.field}: ${it.message}" }

    /**
     * 是否有指定字段的错误
     */
    fun hasError(field: String): Boolean =
        errors.any { it.field == field }

    /**
     * 获取指定字段的错误
     */
    fun getFieldErrors(field: String): List<ValidationError> =
        errors.filter { it.field == field }
}

/**
 * 验证错误信息
 */
data class ValidationError(
    val field: String,
    val message: String
)

// ========== 扩展函数 - 提供便捷的调用方式 ==========

/**
 * 快速验证扩展函数
 */
fun <T> T.validateQuick(): ValidationResult =
    ValidationHelper.validateQuick(this)

/**
 * 完整验证扩展函数
 */
fun <T> T.validateComplete(): ValidationResult =
    ValidationHelper.validateComplete(this)

/**
 * 快速验证并抛出异常的扩展函数
 */
fun <T> T.validateQuickOrThrow(exceptionProvider: (String) -> Exception = ::IllegalArgumentException): T {
    ValidationHelper.validateQuickOrThrow(this, exceptionProvider)
    return this
}

/**
 * 完整验证并抛出异常的扩展函数
 */
fun <T> T.validateCompleteOrThrow(exceptionProvider: (String) -> Exception = ::IllegalArgumentException): T {
    ValidationHelper.validateCompleteOrThrow(this, exceptionProvider)
    return this
}

// ========== 便捷的基类和接口 ==========

/**
 * 值对象基类 - 自动进行快速验证
 * 适用于不可变的值对象，构造时验证一次即可
 */
abstract class ValidatedValueObject {
    init {
        validateQuickOrThrow()
    }
}

/**
 * 可验证接口
 */
interface Validatable {
    /**
     * 快速验证
     */
    fun validate(): ValidationResult = ValidationHelper.validateQuick(this)

    /**
     * 完整验证
     */
    fun validateAll(): ValidationResult = ValidationHelper.validateComplete(this)

    /**
     * 验证并抛出异常
     */
    fun validateOrThrow() {
        ValidationHelper.validateQuickOrThrow(this)
    }
}

// ========== 特殊场景的工具函数 ==========

/**
 * 批量验证多个对象
 */
fun validateMultiple(vararg objects: Any): ValidationResult {
    val allErrors = mutableListOf<ValidationError>()

    objects.forEachIndexed { index, obj ->
        val result = ValidationHelper.validateComplete(obj)
        if (!result.isValid) {
            // 为每个对象的错误添加前缀以区分
            val prefixedErrors = result.errors.map { error ->
                ValidationError("object[$index].${error.field}", error.message)
            }
            allErrors.addAll(prefixedErrors)
        }
    }

    return if (allErrors.isEmpty()) {
        ValidationResult.success()
    } else {
        ValidationResult.failure(allErrors)
    }
}

/**
 * 条件验证 - 满足条件时才进行验证
 */
inline fun <T> T.validateIf(condition: (T) -> Boolean): ValidationResult {
    return if (condition(this)) {
        ValidationHelper.validateQuick(this)
    } else {
        ValidationResult.success()
    }
}