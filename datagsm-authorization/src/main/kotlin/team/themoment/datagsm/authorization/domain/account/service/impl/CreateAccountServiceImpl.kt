package team.themoment.datagsm.authorization.domain.account.service.impl

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.account.service.CheckEmailService
import team.themoment.datagsm.authorization.domain.account.service.CreateAccountService
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.dto.account.request.CreateAccountReqDto
import team.themoment.datagsm.common.dto.account.request.EmailCodeReqDto
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class CreateAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val checkEmailService: CheckEmailService,
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val studentJpaRepository: StudentJpaRepository,
) : CreateAccountService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        consumeEmailCode(reqDto.email, reqDto.code)
        val student = findStudentByEmail(reqDto.email)
        val newAccount =
            AccountJpaEntity.create(reqDto.email).apply {
                password = passwordEncoder.encode(reqDto.password).toString()
                this.student = student
                role = AccountRole.USER
            }
        return accountJpaRepository.save(newAccount)
    }

    private fun findStudentByEmail(email: String) =
        try {
            studentJpaRepository.findByEmail(email).orElse(null).also {
                if (it == null) {
                    log.warn("Student not found for email: $email")
                } else {
                    log.info("Successfully found student for email: $email - ID: ${it.id}, Name: ${it.name}")
                }
            }
        } catch (e: Exception) {
            log.error("Failed to find student for email: $email", e)
            null
        }

    private fun consumeEmailCode(
        email: String,
        code: String,
    ) {
        checkEmailService.execute(EmailCodeReqDto(email, code))
        emailCodeRedisRepository.deleteById(email)
    }
}
