package team.themoment.datagsm.domain.project.repository.custom.impl

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.entity.QProjectJpaEntity.Companion.projectJpaEntity
import team.themoment.datagsm.domain.project.repository.custom.ProjectJpaCustomRepository

@Repository
class ProjectJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProjectJpaCustomRepository {
    override fun searchProjectWithPaging(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity> {
        var searchResult = executeSearch(projectId, projectName, clubId, pageable, projectJpaEntity.projectName::startsWith)
        if (searchResult.content.isEmpty() && projectName != null) {
            searchResult = executeSearch(projectId, projectName, clubId, pageable, projectJpaEntity.projectName::contains)
        }
        return searchResult
    }

    private fun executeSearch(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
        nameMatcher: (String) -> BooleanExpression,
    ): Page<ProjectJpaEntity> {
        val content =
            jpaQueryFactory
                .select(projectJpaEntity)
                .from(projectJpaEntity)
                .leftJoin(projectJpaEntity.projectOwnerClub)
                .fetchJoin()
                .where(
                    projectId?.let { projectJpaEntity.projectId.eq(it) },
                    projectName?.let { nameMatcher(it) },
                    clubId?.let { projectJpaEntity.projectOwnerClub.id.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(projectJpaEntity.count())
                .from(projectJpaEntity)
                .where(
                    projectId?.let { projectJpaEntity.projectId.eq(it) },
                    projectName?.let { nameMatcher(it) },
                    clubId?.let { projectJpaEntity.projectOwnerClub.id.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
