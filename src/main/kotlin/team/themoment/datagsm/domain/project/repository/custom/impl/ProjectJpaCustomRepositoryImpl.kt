package team.themoment.datagsm.domain.project.repository.custom.impl

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
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity> {
        var searchResult = searchProjectWithCondition(id, name, clubId, pageable, useStartsWith = true)
        if (searchResult.content.isEmpty() && name != null) {
            searchResult = searchProjectWithCondition(id, name, clubId, pageable, useStartsWith = false)
        }
        return searchResult
    }

    private fun searchProjectWithCondition(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
        useStartsWith: Boolean,
    ): Page<ProjectJpaEntity> {
        val content =
            jpaQueryFactory
                .select(projectJpaEntity)
                .from(projectJpaEntity)
                .leftJoin(projectJpaEntity.club)
                .fetchJoin()
                .where(
                    projectId?.let { projectJpaEntity.id.eq(it) },
                    projectName?.let {
                        if (useStartsWith) projectJpaEntity.name.startsWith(it) else projectJpaEntity.name.contains(it)
                    },
                    clubId?.let { projectJpaEntity.club.id.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(projectJpaEntity.count())
                .from(projectJpaEntity)
                .where(
                    projectId?.let { projectJpaEntity.id.eq(it) },
                    projectName?.let {
                        if (useStartsWith) projectJpaEntity.name.startsWith(it) else projectJpaEntity.name.contains(it)
                    },
                    clubId?.let { projectJpaEntity.club.id.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
