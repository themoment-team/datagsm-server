package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.security.util.IdpSessionCookieFactory
import team.themoment.datagsm.common.global.security.util.OpaqueTokenHashUtil
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.DeleteIdpSessionService
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteIdpSessionServiceImpl(
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val oauthEnvironment: OauthEnvironment,
) : DeleteIdpSessionService {
    override fun execute(
        targetSessionHandle: String,
        sessionId: String?,
    ): ResponseEntity<Void> {
        val currentSession =
            sessionId
                ?.takeIf { it.isNotBlank() }
                ?.let { idpSessionRedisRepository.findByIdOrNull(it) }
                ?: throw ExpectedException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        // targetSessionHandle은 목록 조회가 내려준 해시값이다.
        // 본인 계정의 세션 안에서만 찾고 이메일도 다시 확인하므로, 다른 계정의 세션은 존재하지 않는 세션과 똑같이 404가 된다.
        // 이메일 인덱스는 TTL 만료 후에도 항목이 남을 수 있어, 인덱스 결과만 믿지 않는다.
        val target =
            idpSessionRedisRepository
                .findAllByEmail(currentSession.email)
                .firstOrNull {
                    it.email == currentSession.email &&
                        OpaqueTokenHashUtil.matches(it.sessionId, targetSessionHandle)
                }
                ?: throw ExpectedException("세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        idpSessionRedisRepository.deleteById(target.sessionId)

        // 자기 세션을 끊었다면 쿠키도 함께 비워야 브라우저에 죽은 세션이 남지 않는다.
        val builder = ResponseEntity.status(HttpStatus.NO_CONTENT)
        if (target.sessionId == currentSession.sessionId) {
            builder.header(
                HttpHeaders.SET_COOKIE,
                IdpSessionCookieFactory.expired(oauthEnvironment).toString(),
            )
        }
        return builder.build()
    }
}
