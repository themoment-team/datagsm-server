package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.dto.response.IdpSessionListResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.IdpSessionResDto
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryIdpSessionService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryIdpSessionServiceImpl(
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
) : QueryIdpSessionService {
    // 세션 쿠키 자체를 자격 증명으로 쓴다. 조회 대상은 그 쿠키가 가리키는 계정으로
    // 한정되므로, 다른 계정의 세션을 볼 수 있는 경로가 생기지 않는다.
    override fun execute(sessionId: String?): IdpSessionListResDto {
        val currentSession =
            sessionId
                ?.takeIf { it.isNotBlank() }
                ?.let { idpSessionRedisRepository.findByIdOrNull(it) }
                ?: throw ExpectedException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val sessions =
            idpSessionRedisRepository
                .findAllByEmail(currentSession.email)
                .sortedByDescending { it.createdAt ?: 0L }
                .map {
                    IdpSessionResDto(
                        sessionId = it.sessionId,
                        userAgent = it.userAgent,
                        createdAt = it.createdAt,
                        current = it.sessionId == currentSession.sessionId,
                    )
                }

        return IdpSessionListResDto(sessions = sessions)
    }
}
