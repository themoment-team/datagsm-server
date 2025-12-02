package team.themoment.datagsm.domain.club.repository.custom.impl

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.QClubJpaEntity.Companion.clubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.custom.ClubJpaCustomRepository

@Repository
class ClubJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ClubJpaCustomRepository {
    override fun searchClubWithPaging(
        id: Long?,
        name: String?,
        type: ClubType?,
        pageable: Pageable,
    ): Page<ClubJpaEntity> {
        var searchResult = searchClubWithStartsWith(id, name, type, pageable)
        if (searchResult.content.isEmpty()) {
            searchResult = searchClubWithContains(id, name, type, pageable)
        }
        return searchResult
    }

    private fun searchClubWithStartsWith(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        pageable: Pageable,
    ): Page<ClubJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    clubJpaEntity,
                    countExpression.`as`("count"),
                ).from(clubJpaEntity)
                .where(
                    clubId?.let { clubJpaEntity.id.eq(it) },
                    clubName?.let { clubJpaEntity.name.startsWith(it) },
                    clubType?.let { clubJpaEntity.type.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val clubs = queryResult.map { it.get(clubJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(clubs, pageable) { count }
    }

    private fun searchClubWithContains(
        id: Long?,
        name: String?,
        type: ClubType?,
        pageable: Pageable,
    ): Page<ClubJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    clubJpaEntity,
                    countExpression.`as`("count"),
                ).from(clubJpaEntity)
                .where(
                    id?.let { clubJpaEntity.id.eq(it) },
                    name?.let { clubJpaEntity.name.contains(it) },
                    type?.let { clubJpaEntity.type.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val clubs = queryResult.map { it.get(clubJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(clubs, pageable) { count }
    }
}
