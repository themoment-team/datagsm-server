package team.themoment.datagsm.domain.project.repository.custom.impl

import com.querydsl.core.types.dsl.Expressions
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
        var searchResult = searchProjectWithStartsWith(projectId, projectName, clubId, pageable)
        if (searchResult.content.isEmpty()) {
            searchResult = searchProjectWithContains(projectId, projectName, clubId, pageable)
        }
        return searchResult
    }

    private fun searchProjectWithStartsWith(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    projectJpaEntity,
                    countExpression.`as`("count"),
                ).from(projectJpaEntity)
                .leftJoin(projectJpaEntity.projectOwnerClub)
                .fetchJoin()
                .where(
                    projectId?.let { projectJpaEntity.projectId.eq(it) },
                    projectName?.let { projectJpaEntity.projectName.startsWith(it) },
                    clubId?.let { projectJpaEntity.projectOwnerClub.clubId.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val projects = queryResult.map { it.get(projectJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(projects, pageable) { count }
    }

    private fun searchProjectWithContains(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    projectJpaEntity,
                    countExpression.`as`("count"),
                ).from(projectJpaEntity)
                .leftJoin(projectJpaEntity.projectOwnerClub)
                .fetchJoin()
                .where(
                    projectId?.let { projectJpaEntity.projectId.eq(it) },
                    projectName?.let { projectJpaEntity.projectName.contains(it) },
                    clubId?.let { projectJpaEntity.projectOwnerClub.clubId.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val projects = queryResult.map { it.get(projectJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(projects, pageable) { count }
    }
}
