package team.themoment.datagsm.global.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.exception.error.ExpectedException

@EnableWebMvc
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    private val objectMapper = ObjectMapper()

    @ExceptionHandler(ExpectedException::class)
    private fun expectedException(ex: ExpectedException): CommonApiResponse<Nothing> {
        logger.warn("ExpectedException : {} ", ex.message)
        logger.trace("ExpectedException Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "An error occurred", ex.statusCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, HttpMessageNotReadableException::class)
    fun validationException(ex: MethodArgumentNotValidException): CommonApiResponse<Nothing> {
        logger.warn("Validation Failed : {}", ex.message)
        logger.trace("Validation Failed Details : ", ex)
        return CommonApiResponse.error(methodArgumentNotValidExceptionToJson(ex), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun validationException(ex: ConstraintViolationException): CommonApiResponse<Nothing> {
        logger.warn("field validation failed : {}", ex.message)
        logger.trace("field validation failed : ", ex)
        return CommonApiResponse.error("field validation failed : ${ex.message}", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun illegalStateException(ex: IllegalStateException): CommonApiResponse<out Unit> {
        if (ex.message?.contains("creationTime key must not be null") == true) {
            logger.warn("Corrupted session detected, treating as invalid session: {}", ex.message)
            return CommonApiResponse.error("Session is invalid or expired", HttpStatus.UNAUTHORIZED)
        }
        return unExpectedException(ex)
    }

    @ExceptionHandler(RuntimeException::class)
    fun unExpectedException(ex: RuntimeException): CommonApiResponse<Nothing> {
        logger.error("UnExpectedException Occur : ", ex)
        return CommonApiResponse.error("internal server error has occurred", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun noHandlerFoundException(ex: NoHandlerFoundException): CommonApiResponse<Nothing> {
        logger.warn("Not Found Endpoint : {}", ex.message)
        logger.trace("Not Found Endpoint Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "Endpoint not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun maxUploadSizeExceededException(ex: MaxUploadSizeExceededException): CommonApiResponse<Nothing> {
        logger.warn("The file is too big : {}", ex.message)
        logger.trace("The file is too big Details : ", ex)
        return CommonApiResponse.error(
            "The file is too big, limited file size : ${ex.maxUploadSize}",
            HttpStatus.BAD_REQUEST,
        )
    }

    private fun methodArgumentNotValidExceptionToJson(ex: MethodArgumentNotValidException): String {
        val globalResults = mutableMapOf<String, Any>()
        val fieldResults = mutableMapOf<String, String?>()

        ex.bindingResult.globalErrors.forEach { error ->
            globalResults[ex.bindingResult.objectName] = error.defaultMessage ?: ""
        }

        ex.bindingResult.fieldErrors.forEach { error ->
            fieldResults[error.field] = error.defaultMessage
        }

        globalResults[ex.bindingResult.objectName] = fieldResults

        return objectMapper.writeValueAsString(globalResults).replace("\"", "'")
    }
}
