package team.themoment.datagsm.oauth.userinfo.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.oauth.userinfo.global.security.authentication.OauthAuthenticationToken
import team.themoment.sdk.exception.ExpectedException

@Component
class CurrentUserProvider(
    private val accountJpaRepository: AccountJpaRepository,
) {
    fun getAuthentication(): OauthAuthenticationToken {
        val authentication: Authentication? =
            SecurityContextHolder
                .getContext()
                .authentication
        if (authentication == null) {
            throw ExpectedException("인증 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (authentication !is OauthAuthenticationToken) {
            throw ExpectedException("인증 정보가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
        }
        return authentication
    }

    fun getCurrentUserEmail(): String = getAuthentication().name

    fun getCurrentAccount(): AccountJpaEntity {
        val email = getCurrentUserEmail()
        return accountJpaRepository
            .findByEmail(email)
            .orElseThrow { ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
    }
}
