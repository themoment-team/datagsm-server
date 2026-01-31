package team.themoment.datagsm.authorization.global.security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import team.themoment.sdk.response.CommonApiResponse
import tools.jackson.databind.ObjectMapper

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorResponse = CommonApiResponse.error("인증에 실패했습니다.", HttpStatus.UNAUTHORIZED)
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
