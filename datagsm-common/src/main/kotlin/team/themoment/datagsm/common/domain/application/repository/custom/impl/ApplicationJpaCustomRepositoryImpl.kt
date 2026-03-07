package team.themoment.datagsm.common.domain.application.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.application.entity.ApplicationJpaEntity
import team.themoment.datagsm.common.domain.application.entity.QApplicationJpaEntity.Companion.applicationJpaEntity
import team.themoment.datagsm.common.domain.application.repository.custom.ApplicationJpaCustomRepository

@Repository
class ApplicationJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ApplicationJpaCustomRepository {
    override fun searchApplicationWithPaging(
        name: String?,
        id: String?,
        pageable: Pageable,
    ): Page<ApplicationJpaEntity> {
        val content =
            jpaQueryFactory
                .selectFrom(applicationJpaEntity)
                .leftJoin(applicationJpaEntity.account)
                .fetchJoin()
                .where(
                    name?.let { applicationJpaEntity.name.startsWith(it) },
                    id?.let { applicationJpaEntity.id.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(applicationJpaEntity.count())
                .from(applicationJpaEntity)
                .where(
                    name?.let { applicationJpaEntity.name.startsWith(it) },
                    id?.let { applicationJpaEntity.id.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
