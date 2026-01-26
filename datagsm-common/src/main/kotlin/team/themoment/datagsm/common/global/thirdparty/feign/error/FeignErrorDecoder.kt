package team.themoment.datagsm.common.global.thirdparty.feign.error

import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import java.io.IOException
import java.nio.charset.StandardCharsets

class FeignErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception {
        val status = response.status()

        if (status >= 400) {
            val request = response.request()
            val errorBody = extractErrorBody(response)

            logger().error(
                "Feign client error occurred while calling {} {} with status {} {}",
                request.httpMethod().name,
                request.url(),
                status,
                response.reason(),
            )
            logger().error("Request headers {}", request.headers())
            try {
                request.body()?.let {
                    logger().error("Request body {}", String(it, StandardCharsets.UTF_8))
                } ?: logger().error("Request body is empty")
            } catch (e: Exception) {
                logger().warn("Failed to log request body", e)
            }
            logger().error("Response headers {}", response.headers())
            logger().error("Response body {}", errorBody)

            val (userMessage, httpStatus) =
                when (status) {
                    400 -> "잘못된 요청입니다." to HttpStatus.BAD_REQUEST
                    401 -> "인증이 필요합니다." to HttpStatus.UNAUTHORIZED
                    403 -> "접근이 거부되었습니다." to HttpStatus.FORBIDDEN
                    404 -> "요청하신 리소스를 찾을 수 없습니다." to HttpStatus.NOT_FOUND
                    429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." to HttpStatus.TOO_MANY_REQUESTS
                    500 -> "외부 서비스 내부 오류가 발생했습니다." to HttpStatus.INTERNAL_SERVER_ERROR
                    502 -> "게이트웨이 오류가 발생했습니다." to HttpStatus.BAD_GATEWAY
                    503 -> "서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해 주세요." to HttpStatus.SERVICE_UNAVAILABLE
                    else -> "외부 요청 처리 중 오류가 발생했습니다." to HttpStatus.INTERNAL_SERVER_ERROR
                }

            throw ExpectedException(userMessage, httpStatus)
        }

        return FeignException.errorStatus(methodKey, response)
    }

    private fun extractErrorBody(response: Response): String =
        try {
            response.body()?.asInputStream()?.let {
                StreamUtils.copyToString(it, StandardCharsets.UTF_8)
            } ?: "Unable to read response body"
        } catch (e: IOException) {
            logger().warn("Failed to read error response body", e)
            "Unable to read response body"
        }
}
