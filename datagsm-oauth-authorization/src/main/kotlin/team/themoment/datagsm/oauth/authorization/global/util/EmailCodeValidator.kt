package team.themoment.datagsm.oauth.authorization.global.util

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.account.entity.EmailCodeRedisEntity
import team.themoment.datagsm.common.domain.account.entity.PasswordResetCodeRedisEntity
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.account.repository.PasswordResetCodeRedisRepository
import team.themoment.sdk.exception.ExpectedException

object EmailCodeValidator {
    fun validateSignupCode(
        email: String,
        code: String,
        emailCodeRedisRepository: EmailCodeRedisRepository,
    ): EmailCodeRedisEntity {
        val emailCode =
            emailCodeRedisRepository
                .findByIdOrNull(email)
                ?: throw ExpectedException("해당 이메일에 인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND)

        if (emailCode.code != code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        return emailCode
    }

    fun validatePasswordResetCode(
        email: String,
        code: String,
        passwordResetCodeRedisRepository: PasswordResetCodeRedisRepository,
    ): PasswordResetCodeRedisEntity {
        val passwordResetCode =
            passwordResetCodeRedisRepository
                .findByIdOrNull(email)
                ?: throw ExpectedException("해당 이메일에 인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND)

        if (passwordResetCode.code != code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }

        return passwordResetCode
    }
}
