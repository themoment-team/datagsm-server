package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.account.service.ApproveTeacherAccountService
import team.themoment.sdk.exception.ExpectedException

@Service
class ApproveTeacherAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
) : ApproveTeacherAccountService {
    @Transactional
    override fun execute(accountId: Long) {
        val account =
            accountJpaRepository.findById(accountId).orElseThrow {
                ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (account.objectType != AccountObjectType.TEACHER) {
            throw ExpectedException("선생님 계정이 아닙니다.", HttpStatus.BAD_REQUEST)
        }

        if (account.status != AccountStatus.PENDING) {
            throw ExpectedException("이미 승인된 계정입니다.", HttpStatus.CONFLICT)
        }

        account.status = AccountStatus.ACTIVE
    }
}
