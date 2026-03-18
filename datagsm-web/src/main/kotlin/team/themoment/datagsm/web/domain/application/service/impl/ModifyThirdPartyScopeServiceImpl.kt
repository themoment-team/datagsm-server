package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.ModifyThirdPartyScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.repository.ThirdPartyScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.ModifyThirdPartyScopeService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyThirdPartyScopeServiceImpl(
    private val thirdPartyScopeJpaRepository: ThirdPartyScopeJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyThirdPartyScopeService {
    @Transactional
    override fun execute(
        applicationId: String,
        scopeId: Long,
        reqDto: ModifyThirdPartyScopeReqDto,
    ): ApplicationResDto {
        val scope =
            thirdPartyScopeJpaRepository.findById(scopeId).orElseThrow {
                ExpectedException("ThirdPartyScope를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (scope.application.id != applicationId) {
            throw ExpectedException("ThirdPartyScope를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        thirdPartyScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)?.let {
            throw ExpectedException(
                "${reqDto.scopeName}은 이미 사용 중인 권한 범위 명칭입니다.",
                HttpStatus.CONFLICT,
            )
        }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (scope.application.account.id != currentAccount.id && !isAdmin) {
            throw ExpectedException("ThirdPartyScope 수정 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        scope.scopeName = reqDto.scopeName
        scope.description = reqDto.description

        return scope.application.toResDto()
    }
}
