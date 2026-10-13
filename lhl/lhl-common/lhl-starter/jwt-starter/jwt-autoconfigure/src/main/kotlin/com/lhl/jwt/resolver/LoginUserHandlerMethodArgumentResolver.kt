package com.lhl.jwt.resolver

import com.lhl.common.core.annotation.CurrentUser
import com.lhl.common.core.helper.UserContext
import com.lhl.jwt.domain.dto.PayloadDTO
import com.lhl.jwt.utils.JwtConstant
import com.lhl.jwt.utils.JwtUtils
import com.nimbusds.jwt.SignedJWT
import org.springframework.core.MethodParameter
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class LoginUserHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
                parameter.parameterType.isAssignableFrom(String::class.java)
    }

    override fun resolveArgument(
        methodParameter: MethodParameter,
        modelAndViewContainer: ModelAndViewContainer?,
        request: NativeWebRequest,
        webDataBinderFactory: WebDataBinderFactory?
    ): Any? {
        val token = getJwtToken(request)
        val signedJWT = SignedJWT.parse(token)
        val username = signedJWT.jwtClaimsSet.subject
        val payloadDTO = JwtUtils.getPayLoadByToken(token, username)
        UserContext.set(payloadDTO.username)
        return payloadDTO.username
    }

    private fun getJwtToken(request: NativeWebRequest): String {
        val authInfo = request.getHeader(JwtConstant.AUTHORIZATION_HEADER)
        if (authInfo.isNullOrBlank()) {
            throw BadCredentialsException(JwtConstant.TOKEN_NOT_EMPTY)
        }
        return authInfo.removePrefix(JwtConstant.AUTHORIZATION_START_STRING)
    }
}