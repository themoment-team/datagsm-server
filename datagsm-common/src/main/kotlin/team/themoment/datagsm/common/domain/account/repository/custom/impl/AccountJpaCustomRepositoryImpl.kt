package team.themoment.datagsm.common.domain.account.repository.custom.impl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.QAccountJpaEntity.Companion.accountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.account.entity.constant.AccountSortBy
import team.themoment.datagsm.common.domain.account.entity.constant.AccountStatus
import team.themoment.datagsm.common.domain.account.repository.custom.AccountJpaCustomRepository
import team.themoment.datagsm.common.global.constant.SortDirection

@Repository
class AccountJpaCustomRepositoryImpl(
    val jpaQueryFactory: JPAQueryFactory,
) : AccountJpaCustomRepository {
    override fun searchAccountsWithPaging(
        email: String?,
        role: AccountRole?,
        objectType: AccountObjectType?,
        status: AccountStatus?,
        pageable: Pageable,
        sortBy: AccountSortBy?,
        sortDirection: SortDirection,
    ): Page<AccountJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val accountIds =
            jpaQueryFactory
                .select(accountJpaEntity.id)
                .from(accountJpaEntity)
                .where(
                    email?.let { accountJpaEntity.email.contains(it) },
                    role?.let { accountJpaEntity.role.eq(it) },
                    objectType?.let { accountJpaEntity.objectType.eq(it) },
                    status?.let { accountJpaEntity.status.eq(it) },
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val content =
            if (accountIds.isEmpty()) {
                emptyList()
            } else {
                jpaQueryFactory
                    .selectFrom(accountJpaEntity)
                    .where(accountJpaEntity.id.`in`(accountIds))
                    .apply { orderSpecifier?.let { orderBy(*it) } }
                    .fetch()
            }

        val countQuery =
            jpaQueryFactory
                .select(accountJpaEntity.count())
                .from(accountJpaEntity)
                .where(
                    email?.let { accountJpaEntity.email.contains(it) },
                    role?.let { accountJpaEntity.role.eq(it) },
                    objectType?.let { accountJpaEntity.objectType.eq(it) },
                    status?.let { accountJpaEntity.status.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun createOrderSpecifier(
        sortBy: AccountSortBy?,
        sortDirection: SortDirection,
    ): Array<OrderSpecifier<*>>? {
        if (sortBy == null) return null

        val path =
            when (sortBy) {
                AccountSortBy.ID -> accountJpaEntity.id
                AccountSortBy.EMAIL -> accountJpaEntity.email
                AccountSortBy.ROLE -> accountJpaEntity.role
                AccountSortBy.CREATED_AT -> accountJpaEntity.createdAt
            }
        return arrayOf(
            when (sortDirection) {
                SortDirection.ASC -> path.asc()
                SortDirection.DESC -> path.desc()
            },
        )
    }
}
