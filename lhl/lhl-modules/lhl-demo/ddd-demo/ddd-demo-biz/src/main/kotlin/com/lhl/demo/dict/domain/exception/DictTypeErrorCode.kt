package com.lhl.demo.dict.domain.exception

import com.lhl.common.core.model.IBaseErrorCode

/**
 * 字典类型错误码
 *
 * code 格式：<PREFIX>_NNNN
 */
enum class DictTypeErrorCode(override val code: String, override val msg: String) : IBaseErrorCode {
    DICT_TYPE_NOT_FOUND("DICT_0001", "字典类型[{0}]不存在"),
    DICT_TYPE_CODE_DUPLICATE("DICT_0002", "字典类型编码[{0}]已存在"),
    DICT_TYPE_HAS_ITEMS("DICT_0003", "字典类型[{0}]下仍存在字典项，无法删除"),
}
