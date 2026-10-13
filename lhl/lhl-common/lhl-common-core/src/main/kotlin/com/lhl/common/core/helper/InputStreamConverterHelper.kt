package com.lhl.common.core.helper

import io.swagger.v3.oas.annotations.tags.Tag
import java.io.*
import java.nio.charset.Charset

/**
 * @className: com.lhl.fw.common.helper.InputStreamConverterHelper
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/12/21 9:34
 **/
@Tag(name = "InputStreamConverterHelper", description = "InputStreamConverterHelper")
object InputStreamConverterHelper {
    // 默认缓冲区大小
    private const val DEFAULT_BUFFER_SIZE = 8192
    // 默认字符编码
    private const val DEFAULT_CHARSET = "UTF-8"

    // 转换为 String
    @Throws(IOException::class)
    fun toString(input: InputStream): String {
        return toString(input, DEFAULT_CHARSET)
    }

    @Throws(IOException::class)
    fun toString(input: InputStream, charset: String): String {
        InputStreamReader(input, charset).use { reader ->
            StringWriter().use { writer ->
                val buffer = CharArray(DEFAULT_BUFFER_SIZE)
                var n: Int
                while (reader.read(buffer).also { n = it } != -1) {
                    writer.write(buffer, 0, n)
                }
                return writer.toString()
            }
        }
    }

    // 转换为 byte 数组
    @Throws(IOException::class)
    fun toByteArray(input: InputStream): ByteArray {
        ByteArrayOutputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n: Int
            while (input.read(buffer).also { n = it } != -1) {
                output.write(buffer, 0, n)
            }
            return output.toByteArray()
        }
    }

    // 转换为指定编码的字符流
    fun toReader(input: InputStream, charset: String): Reader {
        return InputStreamReader(input, Charset.forName(charset))
    }
}
