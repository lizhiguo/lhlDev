package com.lhl.jwt.sample.controller

import com.lhl.common.core.annotation.CurrentUser
import com.lhl.common.core.model.ApiResult
import com.lhl.jwt.JwtUser
import com.lhl.jwt.service.JwtTokenService
import com.lhl.jwt.sample.service.UserAccountService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 登录后拿令牌、看当前身份。
 *
 * 登录 / 刷新 / 登出三类端点由 jwt-starter 内置，不需要在这里实现：
 * - POST /login            账号密码，token 写在响应头 Authorization
 * - POST /mobileCodeLogin  手机 + 验证码
 * - POST /wxLogin          微信
 * - POST /refresh          用 refreshToken 换新的令牌对
 * - POST /logout           加入黑名单，立即失效
 */
@RestController
class AuthController(
    private val jwtTokenService: JwtTokenService,
    private val userAccountService: UserAccountService
) {

    /**
     * 内置 /login 只把 accessToken 放进响应头，refreshToken 拿不到；
     * 这个端点返回完整令牌对，便于前端同时保存两个 token。
     */
    @PostMapping("/auth/token")
    fun token(@CurrentUser user: UserDetails?): ApiResult<Any> {
        if (user == null) {
            return ApiResult.fail(code = "401", msg = "未认证")
        }
        return ApiResult.success(data = jwtTokenService.createTokenPair(user))
    }

    /** 当前登录用户：账号、主键、角色权限，token 非法或被拉黑时返回 401 */
    @GetMapping("/me")
    fun me(@CurrentUser user: UserDetails?): ApiResult<Any> {
        if (user == null) {
            return ApiResult.fail(code = "401", msg = "未认证")
        }
        val profile = userAccountService.findUser(user.username)
        return ApiResult.success(
            data = mapOf(
                "username" to user.username,
                "userId" to (user as? JwtUser)?.userId,
                "nickname" to profile?.nickname,
                "mobile" to profile?.mobile,
                "authorities" to user.authorities.map { it.authority }
            )
        )
    }

    /** 白名单端点，无需认证 */
    @GetMapping("/public/hello")
    fun hello(): ApiResult<Any> = ApiResult.success(data = "hello, jwt starter")
}