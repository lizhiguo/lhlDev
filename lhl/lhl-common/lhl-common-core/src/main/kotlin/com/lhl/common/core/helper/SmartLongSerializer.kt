package com.lhl.common.core.helper


import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer

/**
 * 智能 Long 序列化器（适用于 Jackson 3.x）：
 * - 超出 JS 安全整数范围 ( > 9007199254740991 ) 的转为 String（如雪花 ID）
 * - 在 JS 安全整数范围内的保持原生数字（如时间戳 13 位、普通小 ID）
 */
class SmartLongSerializer : StdSerializer<Long>(Long::class.java) {

    // JS Number.MAX_SAFE_INTEGER = 9007199254740991
    private val maxSafeInteger = 9007199254740991L
    private val minSafeInteger = -9007199254740991L

    override fun serialize(value: Long?, gen: JsonGenerator, ctxt: SerializationContext?) {
        if (value == null) {
            gen.writeNull()
            return
        }

        if (value in minSafeInteger..maxSafeInteger) {
            gen.writeNumber(value)
        } else {
            gen.writeString(value.toString())
        }
    }
}