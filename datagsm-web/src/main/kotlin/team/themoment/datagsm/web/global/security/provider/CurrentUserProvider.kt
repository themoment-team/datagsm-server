package team.themoment.datagsm.web.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.web.global.security.authentication.CustomAuthenticationToken
import team.themoment.datagsm.web.global.security.authentication.principal.CustomPrincipal
import team.themoment.sdk.exception.ExpectedException

@Component
class CurrentUserProvider(
    private val accountJpaRepository: AccountJpaRepository,
) {
    fun getAuthentication(): CustomAuthenticationToken {
        val authentication: Authentication? =
            SecurityContextHolder
                .getContext()
                .authentication
        if (authentication == null) {
            throw ExpectedException("인증 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (authentication !is CustomAuthenticationToken) {
            throw ExpectedException("인증 정보가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
        }
        return authentication
    }

    fun getPrincipal(): CustomPrincipal = getAuthentication().principal

    fun getCurrentUserEmail(): String = getAuthentication().name

    fun getCurrentAccount(): AccountJpaEntity {
        val email = getCurrentUserEmail()
        return accountJpaRepository
            .findByEmail(email)
            .orElseThrow { ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
    }
}
