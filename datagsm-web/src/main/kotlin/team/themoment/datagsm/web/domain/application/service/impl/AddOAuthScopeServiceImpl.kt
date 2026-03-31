package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.dto.request.AddOAuthScopeReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.common.domain.application.repository.OAuthScopeJpaRepository
import team.themoment.datagsm.web.domain.application.service.AddOAuthScopeService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class AddOAuthScopeServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val oauthScopeJpaRepository: OAuthScopeJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : AddOAuthScopeService {
    @Transactional
    override fun execute(
        applicationId: String,
        reqDto: AddOAuthScopeReqDto,
    ): ApplicationResDto {
        val application =
            applicationJpaRepository.findById(applicationId).orElseThrow {
                ExpectedException("Application을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (application.account.id != currentAccount.id && !isAdmin) {
            throw ExpectedException("OAuthScope 추가 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        oauthScopeJpaRepository.findByApplicationIdAndScopeName(applicationId, reqDto.scopeName)?.let {
            throw ExpectedException(
                "${reqDto.scopeName}은 이미 사용 중인 권한 범위 명칭입니다.",
                HttpStatus.CONFLICT,
            )
        }

        val scope =
            OAuthScopeJpaEntity().apply {
                scopeName = reqDto.scopeName
                description = reqDto.description
                this.application = application
            }

        val savedScope = oauthScopeJpaRepository.save(scope)
        application.oauthScopes.add(savedScope)

        return application.toResDto()
    }
}
