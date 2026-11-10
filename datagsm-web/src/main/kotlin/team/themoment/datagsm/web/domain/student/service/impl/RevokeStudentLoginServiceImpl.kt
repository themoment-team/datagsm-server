package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.oauth.repository.IdpSessionRedisRepository
import team.themoment.datagsm.common.domain.oauth.repository.OauthRefreshTokenRedisRepository
import team.themoment.datagsm.web.domain.student.service.RevokeStudentLoginService

/**
 * 정보 수정 요청 대상 학생의 SSO 세션과 refresh token을 끊어, 다음 로그인 때 수정 절차를 밟게 한다.
 *
 * 이미 발급된 access token은 JWT라 만료(기본 1시간)까지 유효하고, 교환 전인 인가 코드도 남는다.
 * 그 구간 이후의 갱신은 인가 서버의 토큰 갱신 판정이 막는다.
 */
@Service
class RevokeStudentLoginServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val idpSessionRedisRepository: IdpSessionRedisRepository,
    private val oauthRefreshTokenRedisRepository: OauthRefreshTokenRedisRepository,
) : RevokeStudentLoginService {
    // 일괄 요청 시 계정 수만큼 Redis를 왕복하므로, 요청 스레드와 DB 커넥션을 붙잡지 않게 비동기로 처리한다.
    @Async
    override fun revokeAsync(studentIds: List<Long>) {
        if (studentIds.isEmpty()) return

        accountJpaRepository
            .findAllByObjectTypeAndObjectIdIn(AccountObjectType.STUDENT, studentIds)
            .forEach { account ->
                idpSessionRedisRepository.deleteAll(idpSessionRedisRepository.findAllByEmail(account.email))
                oauthRefreshTokenRedisRepository.deleteAll(oauthRefreshTokenRedisRepository.findAllByEmail(account.email))
            }
    }
}
