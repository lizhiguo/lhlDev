package com.lhl.common.api.converter

import cn.hutool.v7.core.text.StrUtil
import org.springframework.core.convert.converter.Converter

/**
 * 空字符串转为null
 *
 */
class StringToNullConverter: Converter<String, String?> {
    override fun convert(source: String): String? {
        return if(StrUtil.isBlank(source)) null else source
    }
}