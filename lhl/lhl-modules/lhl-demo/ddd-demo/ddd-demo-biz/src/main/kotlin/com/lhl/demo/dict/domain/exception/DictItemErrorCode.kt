package com.lhl.demo.dict.domain.exception

import com.lhl.common.core.model.IBaseErrorCode

/**
 * 字典项错误码
 */
enum class DictItemErrorCode(override val code: String, override val msg: String) : IBaseErrorCode {
    DICT_ITEM_NOT_FOUND("DICT_ITEM_0001", "字典项[{0}]不存在"),
    DICT_ITEM_CODE_DUPLICATE("DICT_ITEM_0002", "字典项编码[{0}]在当前类型下已存在"),
}
