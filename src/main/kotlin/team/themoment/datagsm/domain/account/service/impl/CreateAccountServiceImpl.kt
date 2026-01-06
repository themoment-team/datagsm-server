package team.themoment.datagsm.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.domain.account.service.CheckEmailService
import team.themoment.datagsm.domain.account.service.CreateAccountService
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class CreateAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val checkEmailService: CheckEmailService,
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
) : CreateAccountService {
    override fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity {
        consumeEmailCode(reqDto.email, reqDto.code)
        val newAccount =
            AccountJpaEntity.create(reqDto.email).apply {
                password = passwordEncoder.encode(reqDto.password)
                student = studentJpaRepository.findByEmail(reqDto.email).orElse(null)
                role = AccountRole.USER
            }
        return accountJpaRepository.save(newAccount)
    }

    private fun consumeEmailCode(
        email: String,
        code: String,
    ) {
        checkEmailService.execute(EmailCodeReqDto(email, code))
        emailCodeRedisRepository.deleteById(email)
    }
}
