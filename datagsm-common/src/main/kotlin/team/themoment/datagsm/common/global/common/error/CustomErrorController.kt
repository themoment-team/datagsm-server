package team.themoment.datagsm.common.global.common.error

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import team.themoment.sdk.response.CommonApiResponse

@Controller
class CustomErrorController : ErrorController {
    @RequestMapping("/error", produces = [MediaType.TEXT_HTML_VALUE])
    fun handleErrorHtml(
        request: HttpServletRequest,
        model: Model,
    ): String {
        val status = getStatus(request)
        val errorMessage = getErrorMessage(status)
        val path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) ?: "Unknown"

        model.addAttribute("statusCode", status.value())
        model.addAttribute("error", status.reasonPhrase)
        model.addAttribute("message", errorMessage)
        model.addAttribute("path", path)

        return "error"
    }

    @RequestMapping("/error", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun handleErrorJson(request: HttpServletRequest): ResponseEntity<CommonApiResponse<Nothing>> {
        val status = getStatus(request)
        val errorMessage = getErrorMessage(status)

        return ResponseEntity
            .status(status)
            .body(CommonApiResponse.error(errorMessage, status))
    }

    private fun getStatus(request: HttpServletRequest): HttpStatus {
        val statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int
        return try {
            HttpStatus.valueOf(statusCode ?: 500)
        } catch (ex: Exception) {
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun getErrorMessage(status: HttpStatus): String =
        when (status) {
            HttpStatus.NOT_FOUND -> "요청하신 페이지를 찾을 수 없습니다"
            HttpStatus.FORBIDDEN -> "접근 권한이 부족합니다"
            HttpStatus.UNAUTHORIZED -> "인증이 필요합니다"
            HttpStatus.BAD_REQUEST -> "잘못된 요청입니다"
            HttpStatus.METHOD_NOT_ALLOWED -> "허용되지 않는 요청 방식입니다"
            HttpStatus.INTERNAL_SERVER_ERROR -> "서버 내부 오류가 발생했습니다"
            HttpStatus.SERVICE_UNAVAILABLE -> "서비스를 일시적으로 사용할 수 없습니다"
            HttpStatus.GATEWAY_TIMEOUT -> "게이트웨이 시간 초과"
            else -> "알 수 없는 오류가 발생했습니다"
        }
}
