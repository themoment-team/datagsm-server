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
class CustomErrorController(
    private val errorMessageResolver: ErrorMessageResolver,
) : ErrorController {
    @RequestMapping("/error", produces = [MediaType.TEXT_HTML_VALUE])
    fun handleErrorHtml(
        request: HttpServletRequest,
        model: Model,
    ): String =
        ((request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int)
            ?.let { HttpStatus.resolve(it) } ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .let { status ->
                model.apply {
                    addAttribute("statusCode", status.value())
                    addAttribute("error", status.reasonPhrase)
                    addAttribute("message", errorMessageResolver.resolveMessage(status))
                    addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) ?: "Unknown")
                }
                "error"
            }

    @RequestMapping("/error", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun handleErrorJson(request: HttpServletRequest): ResponseEntity<CommonApiResponse<Nothing>> =
        ((request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int)
            ?.let { HttpStatus.resolve(it) } ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .let { status ->
                ResponseEntity
                    .status(status)
                    .body(CommonApiResponse.error(errorMessageResolver.resolveMessage(status), status))
            }
}
