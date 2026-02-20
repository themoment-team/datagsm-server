package team.themoment.datagsm.common.global.common.error

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ErrorMessageResolver {
    fun resolveMessage(status: HttpStatus): String =
        when (status) {
            HttpStatus.NOT_FOUND -> "요청하신 페이지를 찾을 수 없습니다"
            HttpStatus.FORBIDDEN -> "접근 권한이 부족합니다"
            HttpStatus.UNAUTHORIZED -> "인증이 필요합니다"
            HttpStatus.BAD_REQUEST -> "잘못된 요청입니다"
            HttpStatus.METHOD_NOT_ALLOWED -> "허용되지 않는 요청 방식입니다"
            HttpStatus.UNSUPPORTED_MEDIA_TYPE -> "지원되지 않는 미디어 타입입니다"
            HttpStatus.CONFLICT -> "요청이 현재 서버 상태와 충돌합니다"
            HttpStatus.TOO_MANY_REQUESTS -> "너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요"
            HttpStatus.INTERNAL_SERVER_ERROR -> "서버 내부 오류가 발생했습니다"
            HttpStatus.SERVICE_UNAVAILABLE -> "서비스를 일시적으로 사용할 수 없습니다"
            HttpStatus.GATEWAY_TIMEOUT -> "게이트웨이 시간 초과"
            else -> "알 수 없는 오류가 발생했습니다"
        }
}
