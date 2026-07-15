package team.themoment.datagsm.common.domain.account.repository.custom

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountSortBy
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.global.constant.SortDirection

interface AccountJpaCustomRepository {
    fun searchAccountsWithPaging(
        email: String?,
        role: AccountRole?,
        objectType: AccountObjectType?,
        status: AccountStatus?,
        pageable: Pageable,
        sortBy: AccountSortBy?,
        sortDirection: SortDirection,
    ): Page<AccountJpaEntity>
}
