package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.DeleteOAuthScopeService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteOAuthScopeServiceImpl(
    private val oauthScopeJpaRepository: OAuthScopeJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteOAuthScopeService {
    @Transactional
    override fun execute(
        applicationId: String,
        scopeId: Long,
    ) {
        val scope =
            oauthScopeJpaRepository.findById(scopeId).orElseThrow {
                ExpectedException("OAuth 권한 범위를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (scope.application.id != applicationId) {
            throw ExpectedException("OAuth 권한 범위를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (scope.application.account.id != currentAccount.id && !isAdmin) {
            throw ExpectedException("OAuth 권한 범위 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        oauthScopeJpaRepository.delete(scope)
    }
}
