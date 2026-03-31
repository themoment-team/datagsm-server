package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.ModifyOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.ModifyOAuthScopeService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyOAuthScopeServiceImpl(
    private val oauthScopeJpaRepository: OAuthScopeJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : ModifyOAuthScopeService {
    @Transactional
    override fun execute(
        applicationId: String,
        scopeId: Long,
        reqDto: ModifyOAuthScopeReqDto,
    ): ApplicationResDto {
        val scope =
            oauthScopeJpaRepository.findById(scopeId).orElseThrow {
                ExpectedException("OAuthScope를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        if (scope.application.id != applicationId) {
            throw ExpectedException("OAuthScope를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        oauthScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)?.let {
            throw ExpectedException(
                "${reqDto.scopeName}은 이미 사용 중인 권한 범위 명칭입니다.",
                HttpStatus.CONFLICT,
            )
        }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (scope.application.account.id != currentAccount.id && !isAdmin) {
            throw ExpectedException("OAuthScope 수정 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        scope.scopeName = reqDto.scopeName
        scope.description = reqDto.description

        return scope.application.toResDto()
    }
}
