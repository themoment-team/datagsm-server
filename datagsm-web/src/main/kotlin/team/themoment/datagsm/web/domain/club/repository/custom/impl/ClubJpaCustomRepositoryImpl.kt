package team.themoment.datagsm.web.domain.club.repository.custom.impl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.ClubSortBy
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.domain.club.QClubJpaEntity.Companion.clubJpaEntity
import team.themoment.datagsm.web.domain.club.repository.custom.ClubJpaCustomRepository
import team.themoment.datagsm.web.global.common.constant.SortDirection

@Repository
class ClubJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ClubJpaCustomRepository {
    override fun searchClubWithPaging(
        id: Long?,
        name: String?,
        type: ClubType?,
        pageable: Pageable,
        sortBy: ClubSortBy?,
        sortDirection: SortDirection,
    ): Page<ClubJpaEntity> {
        var searchResult = searchClubWithCondition(id, name, type, pageable, sortBy, sortDirection, useStartsWith = true)
        if (searchResult.content.isEmpty() && name != null) {
            searchResult = searchClubWithCondition(id, name, type, pageable, sortBy, sortDirection, useStartsWith = false)
        }
        return searchResult
    }

    private fun searchClubWithCondition(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        pageable: Pageable,
        sortBy: ClubSortBy?,
        sortDirection: SortDirection,
        useStartsWith: Boolean,
    ): Page<ClubJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .selectFrom(clubJpaEntity)
                .where(
                    clubId?.let { clubJpaEntity.id.eq(it) },
                    clubName?.let {
                        if (useStartsWith) clubJpaEntity.name.startsWith(it) else clubJpaEntity.name.contains(it)
                    },
                    clubType?.let { clubJpaEntity.type.eq(it) },
                ).apply {
                    orderSpecifier?.let { orderBy(it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        val countQuery =
            jpaQueryFactory
                .select(clubJpaEntity.count())
                .from(clubJpaEntity)
                .where(
                    clubId?.let { clubJpaEntity.id.eq(it) },
                    clubName?.let {
                        if (useStartsWith) clubJpaEntity.name.startsWith(it) else clubJpaEntity.name.contains(it)
                    },
                    clubType?.let { clubJpaEntity.type.eq(it) },
                )
        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun createOrderSpecifier(
        sortBy: ClubSortBy?,
        sortDirection: SortDirection,
    ): OrderSpecifier<*>? {
        if (sortBy == null) return null

        val path =
            when (sortBy) {
                ClubSortBy.ID -> clubJpaEntity.id
                ClubSortBy.NAME -> clubJpaEntity.name
                ClubSortBy.TYPE -> clubJpaEntity.type
            }

        return when (sortDirection) {
            SortDirection.ASC -> path.asc()
            SortDirection.DESC -> path.desc()
        }
    }
}
