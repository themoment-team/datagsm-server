package team.themoment.datagsm.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.exception.error.ExpectedException

@Component
class CurrentUserProvider(
    private val accountJpaRepository: AccountJpaRepository,
) {
    fun getCurrentUserEmail(): String {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw ExpectedException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED)
        return authentication.name
    }

    fun getCurrentAccount(): AccountJpaEntity {
        val email = getCurrentUserEmail()
        return accountJpaRepository
            .findByEmail(email)
            .orElseThrow { ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
    }

    fun getCurrentStudent(): StudentJpaEntity {
        val account = getCurrentAccount()
        return account.student
            ?: throw ExpectedException("학생 정보가 없습니다.", HttpStatus.BAD_REQUEST)
    }
}
