package team.themoment.datagsm.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.exception.error.ExpectedException
import team.themoment.datagsm.global.security.jwt.JwtProvider

@Component
class CurrentUserProvider(
    private val jwtProvider: JwtProvider,
    private val accountJpaRepository: AccountJpaRepository,
) {
    fun getCurrentUserEmail(authorization: String): String {
        val token = authorization.removePrefix("Bearer ")
        return jwtProvider.getEmailFromToken(token)
    }

    fun getCurrentAccount(authorization: String): AccountJpaEntity {
        val email = getCurrentUserEmail(authorization)
        return accountJpaRepository
            .findByAccountEmail(email)
            .orElseThrow { ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
    }

    fun getCurrentStudent(authorization: String): StudentJpaEntity {
        val account = getCurrentAccount(authorization)
        return account.accountStudent
            ?: throw ExpectedException("학생 정보가 없습니다.", HttpStatus.BAD_REQUEST)
    }
}