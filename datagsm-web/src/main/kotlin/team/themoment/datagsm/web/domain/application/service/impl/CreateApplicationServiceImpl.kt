package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.application.dto.request.CreateApplicationReqDto
import team.themoment.datagsm.common.domain.application.dto.response.ApplicationResDto
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.CreateApplicationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import java.util.UUID

@Service
class CreateApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : CreateApplicationService {
    @Transactional
    override fun execute(reqDto: CreateApplicationReqDto): ApplicationResDto {
        val currentAccount = currentUserProvider.getCurrentAccount()

        val application =
            ApplicationJpaEntity().apply {
                id = UUID.randomUUID().toString()
                name = reqDto.name
                account = currentAccount
            }

        reqDto.scopes.forEach { scopeReq ->
            val scopeEntity =
                ThirdPartyScopeJpaEntity().apply {
                    scopeName = scopeReq.scopeName
                    description = scopeReq.description
                    this.application = application
                }
            application.thirdPartyScopes.add(scopeEntity)
        }

        applicationJpaRepository.save(application)

        return application.toResDto()
    }
}

internal fun ApplicationJpaEntity.toResDto(): ApplicationResDto =
    ApplicationResDto(
        id = id,
        name = name,
        accountId = account.id!!,
        scopes =
            thirdPartyScopes.map { scope ->
                ApplicationResDto.ScopeResDto(
                    id = scope.id!!,
                    scopeName = scope.scopeName,
                    description = scope.description,
                )
            },
    )
