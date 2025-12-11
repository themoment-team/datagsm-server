package team.themoment.datagsm.domain.club.repository.custom.impl

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
        var searchResult = searchClubWithCondition(id, name, type, pageable, useStartsWith = true)
        if (searchResult.content.isEmpty() && name != null) {
            searchResult = searchClubWithCondition(id, name, type, pageable, useStartsWith = false)
        }
        return searchResult
    }

    private fun searchClubWithCondition(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        pageable: Pageable,
        useStartsWith: Boolean,
    ): Page<ClubJpaEntity> {
        val content =
            jpaQueryFactory
                .selectFrom(clubJpaEntity)
                .where(
                    clubId?.let { clubJpaEntity.id.eq(it) },
                    clubName?.let {
                        if (useStartsWith) clubJpaEntity.name.startsWith(it) else clubJpaEntity.name.contains(it)
                    },
                    clubType?.let { clubJpaEntity.type.eq(it) },
                ).offset(pageable.offset)
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
}
