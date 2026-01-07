package team.themoment.datagsm.resource.domain.project.repository.custom.impl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.project.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.ProjectSortBy
import team.themoment.datagsm.common.domain.project.QProjectJpaEntity.Companion.projectJpaEntity
import team.themoment.datagsm.resource.domain.project.repository.custom.ProjectJpaCustomRepository
import team.themoment.datagsm.resource.global.common.constant.SortDirection

@Repository
class ProjectJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ProjectJpaCustomRepository {
    override fun searchProjectWithPaging(
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
        sortBy: ProjectSortBy?,
        sortDirection: SortDirection,
    ): Page<ProjectJpaEntity> {
        var searchResult = searchProjectWithCondition(id, name, clubId, pageable, sortBy, sortDirection, useStartsWith = true)
        if (searchResult.content.isEmpty() && name != null) {
            searchResult = searchProjectWithCondition(id, name, clubId, pageable, sortBy, sortDirection, useStartsWith = false)
        }
        return searchResult
    }

    private fun searchProjectWithCondition(
        projectId: Long?,
        projectName: String?,
        clubId: Long?,
        pageable: Pageable,
        sortBy: ProjectSortBy?,
        sortDirection: SortDirection,
        useStartsWith: Boolean,
    ): Page<ProjectJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .select(projectJpaEntity)
                .distinct()
                .from(projectJpaEntity)
                .leftJoin(projectJpaEntity.club)
                .fetchJoin()
                .leftJoin(projectJpaEntity.participants)
                .fetchJoin()
                .where(
                    projectId?.let { projectJpaEntity.id.eq(it) },
                    projectName?.let {
                        if (useStartsWith) projectJpaEntity.name.startsWith(it) else projectJpaEntity.name.contains(it)
                    },
                    clubId?.let { projectJpaEntity.club.id.eq(it) },
                ).apply {
                    orderSpecifier?.let { orderBy(it) }
                }.offset(pageable.offset)
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

    private fun createOrderSpecifier(
        sortBy: ProjectSortBy?,
        sortDirection: SortDirection,
    ): OrderSpecifier<*>? {
        if (sortBy == null) return null

        val path =
            when (sortBy) {
                ProjectSortBy.ID -> projectJpaEntity.id
                ProjectSortBy.NAME -> projectJpaEntity.name
            }

        return when (sortDirection) {
            SortDirection.ASC -> path.asc()
            SortDirection.DESC -> path.desc()
        }
    }
}
