package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.dto.request.CreateApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.CreateApplicationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException
import java.util.UUID

@Service
class CreateApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateApplicationService {
    @Transactional
    override fun execute(reqDto: CreateApplicationReqDto): ApplicationResDto {
        val currentAccount = currentUserProvider.getCurrentAccount()

        val duplicateScopeNames =
            reqDto.scopes
                .groupingBy { it.scopeName }
                .eachCount()
                .filter { it.value > 1 }
                .keys
        if (duplicateScopeNames.isNotEmpty()) {
            throw ExpectedException(
                "${duplicateScopeNames.joinToString(", ")}은 이미 사용 중인 권한 범위 명칭입니다",
                HttpStatus.CONFLICT,
            )
        }

        val application =
            ApplicationJpaEntity().apply {
                id = UUID.randomUUID().toString()
                name = reqDto.name
                account = currentAccount
            }

        reqDto.scopes.forEach { scopeReq ->
            val scopeEntity =
                OAuthScopeJpaEntity().apply {
                    scopeName = scopeReq.scopeName
                    description = scopeReq.description
                    this.application = application
                }
            application.oauthScopes.add(scopeEntity)
        }

        val savedApplication = applicationJpaRepository.save(application)

        return savedApplication.toResDto()
    }
}

internal fun ApplicationJpaEntity.toResDto(): ApplicationResDto =
    ApplicationResDto(
        id = id,
        name = name,
        accountId = account.id!!,
        scopes =
            oauthScopes.map { scope ->
                ApplicationResDto.ScopeResDto(
                    id = scope.id!!,
                    scopeName = scope.scopeName,
                    description = scope.description,
                )
            },
    )
