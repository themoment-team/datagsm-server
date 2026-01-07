package team.themoment.datagsm.authorization.domain.account.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.AccountJpaEntity
import java.util.Optional

@Repository
interface AccountJpaRepository : JpaRepository<AccountJpaEntity, Long> {
    fun findByEmail(email: String): Optional<AccountJpaEntity>
}
