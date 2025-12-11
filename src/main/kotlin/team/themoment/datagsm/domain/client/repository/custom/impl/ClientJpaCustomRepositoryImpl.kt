package team.themoment.datagsm.domain.client.repository.custom.impl

import com.querydsl.core.types.dsl.Expressions
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
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    clientJpaEntity,
                    countExpression.`as`("count"),
                ).from(clientJpaEntity)
                .where(
                    name?.let { clientJpaEntity.name.startsWith(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val clients = queryResult.map { it.get(clientJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(clients, pageable) { count }
    }
}
