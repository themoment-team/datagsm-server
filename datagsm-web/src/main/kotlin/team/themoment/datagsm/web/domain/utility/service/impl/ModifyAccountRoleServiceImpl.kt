package team.themoment.datagsm.web.domain.utility.service.impl

import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.utility.service.ModifyAccountRoleService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.response.CommonApiResponse

@Service
@Profile("!prod")
class ModifyAccountRoleServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
) : ModifyAccountRoleService {
    @Transactional
    override fun execute(
        email: String,
        role: AccountRole,
    ): CommonApiResponse<Nothing> {
        val account =
            accountJpaRepository.findByEmail(email).orElseThrow {
                ExpectedException("해당 이메일에 해당하는 계정이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
            }
        account.role = role
        return CommonApiResponse.success("계정 권한이 변경되었습니다.")
    }
}
