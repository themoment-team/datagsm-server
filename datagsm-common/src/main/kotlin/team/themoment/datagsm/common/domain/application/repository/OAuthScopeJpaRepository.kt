package team.themoment.datagsm.common.domain.application.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.application.entity.OAuthScopeJpaEntity

interface OAuthScopeJpaRepository : JpaRepository<OAuthScopeJpaEntity, Long> {
    fun findByApplicationIdAndScopeName(
        applicationId: String,
        scopeName: String,
    ): OAuthScopeJpaEntity?

    fun findAllByApplicationIdIn(applicationIds: Set<String>): List<OAuthScopeJpaEntity>
}
