package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.ModifyApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.ModifyApplicationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyApplicationService {
    @Transactional
    override fun execute(
        id: String,
        reqDto: ModifyApplicationReqDto,
    ): ApplicationResDto {
        val application =
            applicationJpaRepository.findById(id).orElseThrow {
                ExpectedException("Application을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (application.account != currentAccount && !isAdmin) {
            throw ExpectedException("Application 수정 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        application.name = reqDto.name

        application.thirdPartyScopes.clear()
        reqDto.scopes.forEach { scopeReq ->
            val scopeEntity =
                ThirdPartyScopeJpaEntity().apply {
                    scopeName = scopeReq.scopeName
                    description = scopeReq.description
                    this.application = application
                }
            application.thirdPartyScopes.add(scopeEntity)
        }

        return application.toResDto()
    }
}
