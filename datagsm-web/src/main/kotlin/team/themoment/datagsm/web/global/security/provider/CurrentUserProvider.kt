package team.themoment.datagsm.web.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.global.security.authentication.WebUserAuthenticationToken
import team.themoment.sdk.exception.ExpectedException

@Component
class CurrentUserProvider(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) {
    fun getAuthentication(): WebUserAuthenticationToken {
        val authentication: Authentication? =
            SecurityContextHolder
                .getContext()
                .authentication
        if (authentication == null) {
            throw ExpectedException("인증 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (authentication !is WebUserAuthenticationToken) {
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

    fun getCurrentStudent(): StudentJpaEntity {
        val account = getCurrentAccount()
        if (account.objectType != AccountObjectType.STUDENT || account.objectId == null) {
            throw ExpectedException("학생 정보가 연결되지 않은 계정입니다.", HttpStatus.FORBIDDEN)
        }
        return studentJpaRepository
            .findById(account.objectId!!)
            .orElseThrow { ExpectedException("학생 정보가 연결되지 않은 계정입니다.", HttpStatus.FORBIDDEN) }
    }
}
