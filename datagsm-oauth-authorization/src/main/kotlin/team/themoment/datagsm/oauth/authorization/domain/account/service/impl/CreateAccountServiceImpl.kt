package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckSignupEmailService
import team.themoment.datagsm.oauth.authorization.domain.account.service.CreateAccountService
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class CreateAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val checkSignupEmailService: CheckSignupEmailService,
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
) : CreateAccountService {
    override fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        consumeEmailCode(reqDto.email, reqDto.code)
        val newAccount =
            AccountJpaEntity.create(reqDto.email).apply {
                password = passwordEncoder.encode(reqDto.password).toString()
                student = studentJpaRepository.findByEmail(reqDto.email).orElse(null)
                role = AccountRole.USER
            }
        return accountJpaRepository.save(newAccount)
    }

    private fun consumeEmailCode(
        email: String,
        code: String,
    ) {
        checkSignupEmailService.execute(EmailCodeReqDto(email, code))
        emailCodeRedisRepository.deleteById(email)
    }
}
