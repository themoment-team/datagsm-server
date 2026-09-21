package team.themoment.datagsm.common.domain.project.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.QProjectEditRequestJpaEntity.Companion.projectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectRequestStatus
import team.themoment.datagsm.common.domain.project.repository.custom.ProjectEditRequestJpaCustomRepository
import team.themoment.datagsm.common.domain.student.entity.QStudentJpaEntity

@Repository
class ProjectEditRequestJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProjectEditRequestJpaCustomRepository {
    private val participant = QStudentJpaEntity("editRequestParticipant")

    override fun searchEditRequestWithPaging(
        requestStatus: ProjectRequestStatus?,
        requestedById: Long?,
        pageable: Pageable,
    ): Page<ProjectEditRequestJpaEntity> {
        val requestIds =
            jpaQueryFactory
                .select(projectEditRequestJpaEntity.id)
                .from(projectEditRequestJpaEntity)
                .where(
                    requestStatus?.let { projectEditRequestJpaEntity.requestStatus.eq(it) },
                    requestedById?.let { projectEditRequestJpaEntity.requestedBy.id.eq(it) },
                ).orderBy(projectEditRequestJpaEntity.requestedAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val content = if (requestIds.isEmpty()) emptyList() else fetchWithAssociations(requestIds)

        val countQuery =
            jpaQueryFactory
                .select(projectEditRequestJpaEntity.count())
                .from(projectEditRequestJpaEntity)
                .where(
                    requestStatus?.let { projectEditRequestJpaEntity.requestStatus.eq(it) },
                    requestedById?.let { projectEditRequestJpaEntity.requestedBy.id.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    override fun findAllByParticipantOrRequester(studentId: Long): List<ProjectEditRequestJpaEntity> {
        val requestIds =
            jpaQueryFactory
                .select(projectEditRequestJpaEntity.id)
                .from(projectEditRequestJpaEntity)
                .leftJoin(projectEditRequestJpaEntity.participants, participant)
                .where(
                    projectEditRequestJpaEntity.requestedBy.id
                        .eq(studentId)
                        .or(participant.id.eq(studentId)),
                ).distinct()
                .fetch()

        return if (requestIds.isEmpty()) emptyList() else fetchWithAssociations(requestIds)
    }

    /**
     * ElementCollection 두 개를 한 쿼리에서 fetchJoin하면 MultipleBagFetchException 또는 카테시안 폭증이 발생하므로
     * 컬렉션별로 쿼리를 나눠 영속성 컨텍스트에 채운다.
     */
    private fun fetchWithAssociations(requestIds: List<Long>): List<ProjectEditRequestJpaEntity> {
        val content =
            jpaQueryFactory
                .selectFrom(projectEditRequestJpaEntity)
                .leftJoin(projectEditRequestJpaEntity.originalProject)
                .fetchJoin()
                .leftJoin(projectEditRequestJpaEntity.requestedBy)
                .fetchJoin()
                .leftJoin(projectEditRequestJpaEntity.club)
                .fetchJoin()
                .where(projectEditRequestJpaEntity.id.`in`(requestIds))
                .orderBy(projectEditRequestJpaEntity.requestedAt.desc())
                .fetch()

        jpaQueryFactory
            .selectFrom(projectEditRequestJpaEntity)
            .leftJoin(projectEditRequestJpaEntity.participants)
            .fetchJoin()
            .where(projectEditRequestJpaEntity.id.`in`(requestIds))
            .fetch()

        jpaQueryFactory
            .selectFrom(projectEditRequestJpaEntity)
            .leftJoin(projectEditRequestJpaEntity.repositories)
            .fetchJoin()
            .where(projectEditRequestJpaEntity.id.`in`(requestIds))
            .fetch()

        jpaQueryFactory
            .selectFrom(projectEditRequestJpaEntity)
            .leftJoin(projectEditRequestJpaEntity.techStacks)
            .fetchJoin()
            .where(projectEditRequestJpaEntity.id.`in`(requestIds))
            .fetch()

        return content
    }
}
