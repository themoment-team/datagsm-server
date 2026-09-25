package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.common.global.security.util.IdpSessionCookieFactory
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.LogoutIdpSessionService

@Service
class LogoutIdpSessionServiceImpl(
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val oauthEnvironment: OauthEnvironment,
) : LogoutIdpSessionService {
    // 세션이 이미 없더라도 204로 응답한다. 존재 여부를 알려주면 쿠키 값만 가진 쪽에
    // 유효한 세션인지 판별할 단서를 주게 되고, 사용자 입장에서도 결과는 동일하다.
    override fun execute(sessionId: String?): ResponseEntity<Void> {
        if (!sessionId.isNullOrBlank()) {
            idpSessionRedisRepository.deleteById(sessionId)
        }

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .header(
                HttpHeaders.SET_COOKIE,
                IdpSessionCookieFactory.expired(oauthEnvironment).toString(),
            ).build()
    }
}
