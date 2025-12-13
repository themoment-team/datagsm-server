package team.themoment.datagsm.domain.client.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.client.entity.ClientJpaEntity
import team.themoment.datagsm.domain.client.entity.QClientJpaEntity.Companion.clientJpaEntity
import team.themoment.datagsm.domain.client.repository.custom.ClientJpaCustomRepository

@Repository
class ClientJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ClientJpaCustomRepository {
    override fun searchClientWithPaging(
        name: String?,
        pageable: Pageable,
    ): Page<ClientJpaEntity> {
        val content =
            jpaQueryFactory
                .selectFrom(clientJpaEntity)
                .where(
                    name?.let { clientJpaEntity.name.startsWith(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(clientJpaEntity.count())
                .from(clientJpaEntity)
                .where(
                    name?.let { clientJpaEntity.name.startsWith(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
