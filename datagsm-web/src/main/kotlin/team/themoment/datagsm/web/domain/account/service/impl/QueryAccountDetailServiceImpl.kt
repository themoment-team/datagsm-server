package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.response.AccountResDto
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.web.domain.account.service.QueryAccountDetailService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryAccountDetailServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val accountObjectResolver: AccountObjectResolver,
) : QueryAccountDetailService {
    @Transactional(readOnly = true)
    override fun execute(accountId: Long): AccountResDto {
        val account =
            accountJpaRepository.findById(accountId).orElseThrow {
                ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val resolved = accountObjectResolver.resolve(account)

        return AccountResDto.from(account, resolved.student, resolved.teacher)
    }
}
