package team.themoment.datagsm.global.common.response.wrapper

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

@RestControllerAdvice
class ApiResponseWrapper : ResponseBodyAdvice<Any> {
    companion object {
        private val NOT_WRAPPING_URL =
            arrayOf(
                "/v3/api-docs/**",
                "/v3/api-docs",
                "/swagger-ui/**",

                "/v1/students/excel/download",
            )
    }

    private val matcher = AntPathMatcher()

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        if (isNotWrappingURL(request.uri.path)) {
            return body
        }

        when (body) {
            is CommonApiResponse<*> -> {
                return byPassResponse(body, response)
            }

            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val bodyMap = body as Map<String, Any>
                val errorResponse = exceptionResponse(response, bodyMap)
                if (errorResponse != null) return errorResponse
            }

            null -> {
                response.setStatusCode(HttpStatus.NO_CONTENT)
                return null
            }
        }

        response.setStatusCode(HttpStatus.OK)
        return CommonApiResponse(
            status = HttpStatus.OK,
            code = HttpStatus.OK.value(),
            message = "OK",
            data = body,
        )
    }

    private fun byPassResponse(
        body: CommonApiResponse<*>,
        response: ServerHttpResponse,
    ): Any {
        response.setStatusCode(body.status)
        return body
    }

    private fun exceptionResponse(
        response: ServerHttpResponse,
        bodyMap: Map<String, Any>,
    ): CommonApiResponse<Nothing>? {
        val statusValue = bodyMap["status"]
        if (statusValue is Int && statusValue in 400..599) {
            val status = HttpStatus.valueOf(statusValue)
            response.setStatusCode(HttpStatusCode.valueOf(statusValue))
            return CommonApiResponse.error(status.reasonPhrase, status)
        }
        return null
    }

    private fun isNotWrappingURL(requestURI: String?): Boolean =
        requestURI?.let { uri ->
            NOT_WRAPPING_URL.any { pattern ->
                matcher.match(pattern, uri)
            }
        } ?: false
}
