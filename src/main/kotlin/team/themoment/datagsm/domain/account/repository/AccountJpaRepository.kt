package team.themoment.datagsm.domain.account.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.account.entity.AccountJpaEntity
import java.util.Optional

@Repository
interface AccountJpaRepository : JpaRepository<AccountJpaEntity, Long> {

    fun findByAccountEmail(email: String): Optional<AccountJpaEntity>

    fun existsByAccountEmail(email: String): Boolean
}