package com.lhl.common.core.helper

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.tags.Tag
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.nio.charset.Charset

/**
 * @className: com.lhl.fw.common.helper.InputStreamConverterHelperTest
 * @description: TODO 类描述
 * @author: 78772
 * @date:  2024/12/21 9:42
 **/
@Tag(name = "InputStreamConverterHelperTest", description = "InputStreamConverterHelperTest")
class InputStreamConverterHelperTest {
    companion object{
        private val log = KotlinLogging.logger {}
    }
    @Test
    fun `InputStream 转字符串`() {
        val inputStream: InputStream = "Hello, Kotlin!".byteInputStream()
        val result = InputStreamConverterHelper.toString(inputStream)
        log.debug { "-------------------------------------" }
        println(result) // 输出: Hello, Kotlin!
    }

    @Test
    fun `InputStream 转字节数组`() {
        val inputStream: InputStream = "Hello, Kotlin!".byteInputStream()
        val byteArray = InputStreamConverterHelper.toByteArray(inputStream)
        println(byteArray.decodeToString()) // 输出: Hello, Kotlin!
    }

    @Test
    fun `InputStream 转Reader`() {
        val inputStream: InputStream = "Hello, Kotlin!".byteInputStream()
        val reader = InputStreamConverterHelper.toReader(inputStream, "UTF-8")
        println(reader.readText()) // 输出: Hello, Kotlin!
    }

    @Test
    @DisplayName("Kotlin 的内置扩展函数")
    fun `Kotlin 内置扩展函数 readBytes`() {
        val inputStream: InputStream = "Hello, Kotlin!".byteInputStream()
        val byteArray = inputStream.readBytes()
        val string = inputStream.bufferedReader(Charset.forName("UTF-8")).use { it.readText() }
    }
}