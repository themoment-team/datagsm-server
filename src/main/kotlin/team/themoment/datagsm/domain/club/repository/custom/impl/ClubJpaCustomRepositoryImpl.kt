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
    private val jpaQueryFactory: JPAQueryFactory
) : ClubJpaCustomRepository {
    override fun searchClubWithPaging(
        clubId: Long?,
        clubName: String?,
        clubType: ClubType?,
        pageable: Pageable
    ): Page<ClubJpaEntity> {
        val content = jpaQueryFactory
            .selectFrom(clubJpaEntity)
            .where(
                clubId?.let { clubJpaEntity.clubId.eq(it) },
                clubName?.let { clubJpaEntity.clubName.contains(it) },
                clubType?.let { clubJpaEntity.clubType.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = jpaQueryFactory
            .select(clubJpaEntity.count())
            .from(clubJpaEntity)
            .where(
                clubId?.let { clubJpaEntity.clubId.eq(it) },
                clubName?.let { clubJpaEntity.clubName.contains(it) },
                clubType?.let { clubJpaEntity.clubType.eq(it) }
            );

        return PageableExecutionUtils.getPage(content,pageable) { countQuery.fetchOne() ?: 0L }
    }
}
