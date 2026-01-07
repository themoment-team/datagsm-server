package team.themoment.datagsm.authorization.global.thirdparty.feign.error

import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils
import team.themoment.sdk.exception.ExpectedException
import java.io.IOException
import java.nio.charset.StandardCharsets

class FeignErrorDecoder : ErrorDecoder {
    private val logger = LoggerFactory.getLogger(FeignErrorDecoder::class.java)

    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception {
        val status = response.status()

        if (status >= 400) {
            val errorBody = extractErrorBody(response)
            val headers = response.headers()
            val url = response.request().url()
            val httpMethod = response.request().httpMethod().name

            logger.error(
                "Feign 클라이언트 오류 - 메서드: {}, HTTP 메서드: {}, URL: {}, 상태: {}, 이유: {}",
                methodKey,
                httpMethod,
                url,
                status,
                response.reason(),
            )
            logger.error("응답 헤더: {}", headers)
            logger.error("응답 본문: {}", errorBody)
            logRequestDetails(response, methodKey)

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
            } ?: "응답 본문을 읽을 수 없습니다"
        } catch (e: IOException) {
            logger.warn("오류 응답 본문을 읽는 데 실패했습니다", e)
            "응답 본문을 읽을 수 없습니다"
        }

    private fun logRequestDetails(
        response: Response,
        methodKey: String,
    ) {
        try {
            val url = response.request().url()
            val method = response.request().httpMethod().name
            val requestHeaders = response.request().headers()

            logger.error("요청 정보 - 메서드: {}, HTTP 메서드: {}, URL: {}", methodKey, method, url)
            logger.error("요청 헤더: {}", requestHeaders)

            response.request().body()?.let { body ->
                val requestBody = String(body, StandardCharsets.UTF_8)
                logger.error("요청 본문: {}", requestBody)
            }
        } catch (e: Exception) {
            logger.warn("요청 상세 로깅에 실패했습니다", e)
        }
    }
}
