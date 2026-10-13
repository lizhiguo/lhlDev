package com.lhl.common.core.model

data class TokenDTO(
    val data: Data,
    val errcode: String,
    val errmsg: String
)

data class Data(
    val CompanyCode: String,
    val IsAdministrator: String,
    val LastAccessed: String,
    val LoginTime: String,
    val UserCode: String,
    val UserId: String,
    val UserInfo: UserInfo,
    val UserName: String,
    val _deptCode: String,
    val _deptName: String,
    val _email: String,
    val _postName: String,
    val _state: String,
    val _telephone: String,
    val expires_in: String,
    val myphone: String
)

data class UserInfo(
    val F_BranchCode: String,
    val F_BranchName: String,
    val F_Code: String,
    val F_Extend1: String,
    val F_Extend2: String,
    val F_Guid: String,
    val F_IDNumber: String,
    val F_IsAdministrator: Boolean,
    val F_IsApproved: Boolean,
    val F_IsBackground: Boolean,
    val F_IsLocked: Boolean,
    val F_IsTimeOut: Boolean,
    val F_Name: String,
    val F_ParentId: String,
    val F_Phone: String,
    val F_PostCode: String,
    val F_PostName: String,
    val F_SAPCode: String,
    val F_Source: String,
    val F_State: Int,
    val F_Status: String,
    val F_WebSite: String,
    val F_WorkTimeA: String
)