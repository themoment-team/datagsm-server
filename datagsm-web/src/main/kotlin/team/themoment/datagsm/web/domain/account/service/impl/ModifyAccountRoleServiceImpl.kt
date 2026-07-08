package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.ModifyAccountRoleReqDto
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.account.service.ModifyAccountRoleService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyAccountRoleServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyAccountRoleService {
    @Transactional
    override fun execute(
        accountId: Long,
        reqDto: ModifyAccountRoleReqDto,
    ) {
        val account =
            accountJpaRepository.findById(accountId).orElseThrow {
                ExpectedException("계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val currentAccount = currentUserProvider.getCurrentAccount()
        if (account.id == currentAccount.id) {
            throw ExpectedException("본인의 권한은 변경할 수 없습니다.", HttpStatus.FORBIDDEN)
        }

        if (account.role == AccountRole.ROOT) {
            throw ExpectedException("최고 관리자 계정의 권한은 변경할 수 없습니다.", HttpStatus.FORBIDDEN)
        }

        account.role = reqDto.role
    }
}
