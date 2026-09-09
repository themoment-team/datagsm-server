package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import java.util.Optional

interface OauthConsentJpaRepository : JpaRepository<OauthConsentJpaEntity, Long> {
    fun findByAccountIdAndClientId(
        accountId: Long,
        clientId: String,
    ): Optional<OauthConsentJpaEntity>
}
