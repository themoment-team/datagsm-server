package team.themoment.datagsm.common.domain.application.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.application.entity.ThirdPartyScopeJpaEntity

interface ThirdPartyScopeJpaRepository : JpaRepository<ThirdPartyScopeJpaEntity, Long> {
    fun findByApplicationIdAndScopeName(
        applicationId: String,
        scopeName: String,
    ): ThirdPartyScopeJpaEntity?
}
