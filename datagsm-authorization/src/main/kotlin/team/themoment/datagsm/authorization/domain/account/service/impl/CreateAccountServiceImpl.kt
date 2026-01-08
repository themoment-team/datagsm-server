package team.themoment.datagsm.authorization.domain.account.service.impl

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.authorization.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.authorization.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.authorization.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.authorization.domain.account.service.CheckEmailService
import team.themoment.datagsm.authorization.domain.account.service.CreateAccountService
import team.themoment.datagsm.authorization.global.thirdparty.feign.resource.ResourceServerClient
import team.themoment.datagsm.authorization.global.thirdparty.feign.resource.ResourceServerProperties
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.AccountRole
import team.themoment.sdk.exception.ExpectedException

@Service
@Transactional
class CreateAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val checkEmailService: CheckEmailService,
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
    private val resourceServerClient: ResourceServerClient,
    private val resourceServerProperties: ResourceServerProperties,
) : CreateAccountService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        consumeEmailCode(reqDto.email, reqDto.code)
        fetchAndLogStudentInfo(reqDto.email)
        val newAccount =
            AccountJpaEntity.create(reqDto.email).apply {
                password = passwordEncoder.encode(reqDto.password).toString()
                student = null
                role = AccountRole.USER
            }
        return accountJpaRepository.save(newAccount)
    }

    private fun fetchAndLogStudentInfo(email: String) {
        try {
            val students = resourceServerClient.getStudentByEmail(resourceServerProperties.apiKey, email)
            val student = students.firstOrNull()
            if (student == null) {
                log.warn("Student not found for email: $email")
            } else {
                log.info("Successfully fetched student info for email: $email - ID: ${student.id}, Name: ${student.name}")
            }
        } catch (e: Exception) {
            log.error("Failed to fetch student from resource server for email: $email", e)
        }
    }

    private fun consumeEmailCode(
        email: String,
        code: String,
    ) {
        checkEmailService.execute(EmailCodeReqDto(email, code))
        emailCodeRedisRepository.deleteById(email)
    }
}
