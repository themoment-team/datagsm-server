package team.themoment.datagsm.authorization.domain.account.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.authorization.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.authorization.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.authorization.domain.account.service.CheckEmailService
import team.themoment.datagsm.authorization.global.exception.error.ExpectedException
import team.themoment.datagsm.authorization.global.security.annotation.EmailRateLimitType
import team.themoment.datagsm.authorization.global.security.annotation.EmailRateLimited

@Service
class CheckEmailServiceImpl(
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
) : CheckEmailService {
    @EmailRateLimited(type = EmailRateLimitType.CHECK_EMAIL)
    override fun execute(reqDto: EmailCodeReqDto) {
        val emailCodeRedisEntity =
            emailCodeRedisRepository
                .findByIdOrNull(reqDto.email)
                ?: throw ExpectedException("해당 이메일에 인증 코드가 존재하지 않습니다.", HttpStatus.NOT_FOUND)

        if (emailCodeRedisEntity.code != reqDto.code) {
            throw ExpectedException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST)
        }
    }
}
