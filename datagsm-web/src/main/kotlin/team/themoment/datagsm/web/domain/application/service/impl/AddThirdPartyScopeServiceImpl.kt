package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.AddThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.AddThirdPartyScopeService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class AddThirdPartyScopeServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val thirdPartyScopeJpaRepository: ThirdPartyScopeJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : AddThirdPartyScopeService {
    @Transactional
    override fun execute(
        applicationId: String,
        reqDto: AddThirdPartyScopeReqDto,
    ): ApplicationResDto {
        val application =
            applicationJpaRepository.findById(applicationId).orElseThrow {
                ExpectedException("Application을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (application.account.id != currentAccount.id && !isAdmin) {
            throw ExpectedException("ThirdPartyScope 추가 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        thirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)?.let {
            throw ExpectedException(
                "${reqDto.scopeName}은 이미 사용 중인 권한 범위 명칭입니다.",
                HttpStatus.CONFLICT,
            )
        }

        val scope =
            ThirdPartyScopeJpaEntity().apply {
                scopeName = reqDto.scopeName
                description = reqDto.description
                this.application = application
            }

        val savedScope = thirdPartyScopeJpaRepository.save(scope)
        application.thirdPartyScopes.add(savedScope)

        return application.toResDto()
    }
}
