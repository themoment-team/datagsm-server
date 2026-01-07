package team.themoment.datagsm.web.global.common.response.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus

data class CommonApiResponse<T>(
    @field:Schema(description = "상태 메시지", nullable = false, example = "OK")
    val status: HttpStatus,
    @field:Schema(description = "상태 코드", nullable = false, example = "200")
    val code: Int,
    @field:Schema(description = "메시지", nullable = false, example = "완료되었습니다.")
    val message: String,
    @field:Schema(description = "데이터", nullable = true)
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val data: T? = null,
) {
    companion object {
        fun success(message: String): CommonApiResponse<Nothing> =
            CommonApiResponse(
                status = HttpStatus.OK,
                code = HttpStatus.OK.value(),
                message = message,
            )

        fun <T> success(
            message: String,
            data: T,
        ): CommonApiResponse<T> =
            CommonApiResponse(
                status = HttpStatus.OK,
                code = HttpStatus.OK.value(),
                message = message,
                data = data,
            )

        fun created(message: String): CommonApiResponse<Nothing> =
            CommonApiResponse(
                status = HttpStatus.CREATED,
                code = HttpStatus.CREATED.value(),
                message = message,
            )

        fun <T> created(
            message: String,
            data: T,
        ): CommonApiResponse<T> =
            CommonApiResponse(
                status = HttpStatus.CREATED,
                code = HttpStatus.CREATED.value(),
                message = message,
                data = data,
            )

        fun error(
            message: String,
            status: HttpStatus,
        ): CommonApiResponse<Nothing> =
            CommonApiResponse(
                status = status,
                code = status.value(),
                message = message,
            )
    }
}
