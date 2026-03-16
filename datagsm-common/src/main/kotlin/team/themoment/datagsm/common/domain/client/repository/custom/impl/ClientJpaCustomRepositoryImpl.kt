package team.themoment.datagsm.common.domain.client.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.common.domain.client.entity.QClientJpaEntity.Companion.clientJpaEntity
import team.themoment.datagsm.common.domain.client.repository.custom.ClientJpaCustomRepository

@Repository
class ClientJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ClientJpaCustomRepository {
    override fun searchClientWithPaging(
        clientName: String?,
        serviceName: String?,
        pageable: Pageable,
    ): Page<ClientJpaEntity> {
        // 1쿼리: 페이지네이션 적용하여 client ID만 조회
        val clientIds =
            jpaQueryFactory
                .select(clientJpaEntity.id)
                .from(clientJpaEntity)
                .where(
                    clientName?.let { clientJpaEntity.clientName.startsWith(it) },
                    serviceName?.let { clientJpaEntity.serviceName.startsWith(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        // 2쿼리: ID IN절로 account fetchJoin
        val content =
            if (clientIds.isEmpty()) {
                emptyList()
            } else {
                jpaQueryFactory
                    .selectFrom(clientJpaEntity)
                    .leftJoin(clientJpaEntity.account).fetchJoin()
                    .where(clientJpaEntity.id.`in`(clientIds))
                    .fetch()
            }

        val countQuery =
            jpaQueryFactory
                .select(clientJpaEntity.count())
                .from(clientJpaEntity)
                .where(
                    clientName?.let { clientJpaEntity.clientName.startsWith(it) },
                    serviceName?.let { clientJpaEntity.serviceName.startsWith(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    override fun findAllByAccountWithPaging(
        account: AccountJpaEntity,
        pageable: Pageable,
    ): Page<ClientJpaEntity> {
        // 1쿼리: 페이지네이션 적용하여 client ID만 조회
        val clientIds =
            jpaQueryFactory
                .select(clientJpaEntity.id)
                .from(clientJpaEntity)
                .where(clientJpaEntity.account.eq(account))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        // 2쿼리: ID IN절로 account fetchJoin
        val content =
            if (clientIds.isEmpty()) {
                emptyList()
            } else {
                jpaQueryFactory
                    .selectFrom(clientJpaEntity)
                    .leftJoin(clientJpaEntity.account).fetchJoin()
                    .where(clientJpaEntity.id.`in`(clientIds))
                    .fetch()
            }

        val countQuery =
            jpaQueryFactory
                .select(clientJpaEntity.count())
                .from(clientJpaEntity)
                .where(clientJpaEntity.account.eq(account))

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
