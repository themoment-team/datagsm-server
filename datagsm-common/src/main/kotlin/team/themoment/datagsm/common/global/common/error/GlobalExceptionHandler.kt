package team.themoment.datagsm.common.global.common.error

import jakarta.validation.ConstraintViolationException
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import team.themoment.datagsm.common.domain.oauth.dto.response.OAuthErrorResDto
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import team.themoment.datagsm.common.global.common.discord.error.DiscordErrorNotificationService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.logging.logger.logger
import team.themoment.sdk.response.CommonApiResponse
import tools.jackson.databind.ObjectMapper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@EnableWebMvc
@RestControllerAdvice
class GlobalExceptionHandler(
    private val discordErrorNotificationService: DiscordErrorNotificationService? = null,
    private val environment: Environment,
) {
    private val objectMapper = ObjectMapper()

    @ExceptionHandler(OAuthException::class)
    fun handleOAuthException(ex: OAuthException): ResponseEntity<OAuthErrorResDto> {
        logger().warn("OAuth Error: {} - {}", ex.error, ex.errorDescription)
        logger().trace("OAuth Error Details: ", ex)

        val errorResponse =
            OAuthErrorResDto(
                error = ex.error,
                errorDescription = ex.errorDescription,
                errorUri = null,
            )

        return ResponseEntity.status(ex.httpStatus).body(errorResponse)
    }

    @ExceptionHandler(ExpectedException::class)
    fun expectedException(ex: ExpectedException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("ExpectedException : {} ", ex.message)
        logger().trace("ExpectedException Details : ", ex)
        return createErrorResponse(ex.statusCode, ex.message ?: "An error occurred")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationException(ex: MethodArgumentNotValidException): ResponseEntity<*> {
        logger().warn("Validation Failed : {}", ex.message)
        logger().trace("Validation Failed Details : ", ex)

        if (isOAuthTokenEndpoint()) {
            val errorResponse =
                OAuthErrorResDto(
                    error = "invalid_request",
                    errorDescription = extractValidationErrorMessage(ex),
                    errorUri = null,
                )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
        }

        return createErrorResponse(HttpStatus.BAD_REQUEST, methodArgumentNotValidExceptionToJson(ex))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("Invalid Request Body : {}", ex.message)
        logger().trace("Invalid Request Body Details : ", ex)
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body format")
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun validationException(ex: ConstraintViolationException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("field validation failed : {}", ex.message)
        logger().trace("field validation failed : ", ex)
        return createErrorResponse(HttpStatus.BAD_REQUEST, "field validation failed : ${ex.message}")
    }

    @ExceptionHandler(AuthorizationDeniedException::class, AccessDeniedException::class)
    fun authorizationDeniedException(ex: Exception): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("Authorization Denied : {}", ex.message)
        logger().trace("Authorization Denied Details : ", ex)
        return createErrorResponse(HttpStatus.FORBIDDEN, "접근 권한이 부족합니다")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun illegalStateException(ex: IllegalStateException): ResponseEntity<CommonApiResponse<Nothing>> {
        if (ex.message?.contains("creationTime key must not be null") == true) {
            logger().warn("Corrupted session detected, treating as invalid session: {}", ex.message)
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Session is invalid or expired")
        }
        return unExpectedException(ex)
    }

    @ExceptionHandler(RuntimeException::class)
    fun unExpectedException(ex: RuntimeException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().error("UnExpectedException Occur : ", ex)

        discordErrorNotificationService?.notifyError(
            exception = ex,
            context = "An unexpected runtime exception occurred in the application.",
            additionalInfo =
                mapOf(
                    "Thread" to Thread.currentThread().name,
                    "Request URI" to getCurrentRequestUri(),
                    "Profile" to getActiveProfile(),
                ),
        )

        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error has occurred")
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun noHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("Not Found Endpoint : {}", ex.message)
        logger().trace("Not Found Endpoint Details : ", ex)
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.message ?: "Endpoint not found")
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun maxUploadSizeExceededException(ex: MaxUploadSizeExceededException): ResponseEntity<CommonApiResponse<Nothing>> {
        logger().warn("The file is too big : {}", ex.message)
        logger().trace("The file is too big Details : ", ex)
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "The file is too big, limited file size : ${ex.maxUploadSize}",
        )
    }

    private fun createErrorResponse(
        status: HttpStatus,
        message: String,
    ): ResponseEntity<CommonApiResponse<Nothing>> =
        ResponseEntity
            .status(status)
            .body(CommonApiResponse.error(message, status))

    private fun methodArgumentNotValidExceptionToJson(ex: MethodArgumentNotValidException): String {
        val result = mutableMapOf<String, Any>()
        val globalErrors = mutableListOf<String>()
        val fieldErrors = mutableMapOf<String, String?>()

        ex.bindingResult.globalErrors.forEach { error ->
            globalErrors.add(error.defaultMessage ?: "")
        }

        ex.bindingResult.fieldErrors.forEach { error ->
            fieldErrors[error.field] = error.defaultMessage
        }

        if (globalErrors.isNotEmpty()) {
            result["globalErrors"] = globalErrors
        }
        if (fieldErrors.isNotEmpty()) {
            result["fieldErrors"] = fieldErrors
        }

        return objectMapper.writeValueAsString(result)
    }

    private fun getCurrentRequestUri(): String =
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            URLDecoder.decode(requestAttributes?.request?.requestURI, StandardCharsets.UTF_8) ?: "Unknown"
        } catch (_: Exception) {
            "Unable to get request URI"
        }

    private fun getActiveProfile(): String = environment.activeProfiles.firstOrNull() ?: "default"

    private fun isOAuthTokenEndpoint(): Boolean {
        val requestUri = getCurrentRequestUri()
        return requestUri == "/v1/oauth/token"
    }

    private fun extractValidationErrorMessage(ex: MethodArgumentNotValidException): String {
        val fieldErrors =
            ex.bindingResult.fieldErrors
                .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val globalErrors =
            ex.bindingResult.globalErrors
                .joinToString(", ") { it.defaultMessage ?: "" }

        return when {
            fieldErrors.isNotEmpty() -> fieldErrors
            globalErrors.isNotEmpty() -> globalErrors
            else -> "Validation failed"
        }
    }
}
