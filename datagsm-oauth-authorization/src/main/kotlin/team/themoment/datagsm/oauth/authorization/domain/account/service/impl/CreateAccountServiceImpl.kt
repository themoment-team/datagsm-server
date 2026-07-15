package team.themoment.datagsm.oauth.authorization.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.repository.EmailCodeRedisRepository
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.domain.teacher.entity.TeacherJpaEntity
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository
import team.themoment.datagsm.oauth.authorization.domain.account.service.CreateAccountService
import team.themoment.datagsm.oauth.authorization.global.util.EmailCodeValidator
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val teacherJpaRepository: TeacherJpaRepository,
    private val emailCodeRedisRepository: EmailCodeRedisRepository,
    private val passwordEncoder: PasswordEncoder,
) : CreateAccountService {
    @Transactional
    override fun execute(reqDto: CreateAccountReqDto): AccountJpaEntity {
        if (accountJpaRepository.findByEmail(reqDto.email).isPresent) {
            throw ExpectedException("이미 해당 이메일을 가진 계정이 존재합니다.", HttpStatus.CONFLICT)
        }

        consumeEmailCode(reqDto.email, reqDto.code)

        val newAccount =
            AccountJpaEntity.create(reqDto.email).apply {
                password = passwordEncoder.encode(reqDto.password).toString()
                role = AccountRole.USER
            }

        when (reqDto.objectType) {
            AccountObjectType.STUDENT -> {
                val student = studentJpaRepository.findByEmail(reqDto.email).orElse(null)
                newAccount.objectId = student?.id
                newAccount.objectType = AccountObjectType.STUDENT
                newAccount.status = AccountStatus.ACTIVE
            }
            AccountObjectType.TEACHER -> {
                val name =
                    reqDto.name?.takeIf { it.isNotBlank() }
                        ?: throw ExpectedException("선생님 가입 시 이름은 필수입니다.", HttpStatus.BAD_REQUEST)
                val department =
                    reqDto.department
                        ?: throw ExpectedException("선생님 가입 시 소속 부서는 필수입니다.", HttpStatus.BAD_REQUEST)
                if (teacherJpaRepository.findByEmail(reqDto.email).isPresent) {
                    throw ExpectedException("이미 해당 이메일을 가진 선생님이 존재합니다.", HttpStatus.CONFLICT)
                }
                val teacher =
                    teacherJpaRepository.save(
                        TeacherJpaEntity.create(name, reqDto.email, department, reqDto.description),
                    )
                newAccount.objectId = teacher.id
                newAccount.objectType = AccountObjectType.TEACHER
                newAccount.status = AccountStatus.PENDING
            }
        }

        return accountJpaRepository.save(newAccount)
    }

    private fun consumeEmailCode(
        email: String,
        code: String,
    ) {
        EmailCodeValidator.validateSignupCode(email, code, emailCodeRedisRepository)
        emailCodeRedisRepository.deleteById(email)
    }
}
