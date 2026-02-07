package team.themoment.datagsm.common.global.security.util

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import team.themoment.sdk.response.CommonApiResponse
import tools.jackson.databind.ObjectMapper

object SecurityFilterResponseUtil {
    fun sendErrorResponse(
        response: HttpServletResponse,
        objectMapper: ObjectMapper,
        message: String,
        status: HttpStatus = HttpStatus.UNAUTHORIZED,
    ) {
        val errorResponse = CommonApiResponse.error(message, status)
        response.status = status.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
