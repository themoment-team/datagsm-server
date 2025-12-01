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
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
    ): Page<ProjectJpaEntity> {
        var searchResult = executeSearch(id, name, clubId, pageable, projectJpaEntity.name::startsWith)
        if (searchResult.content.isEmpty() && name != null) {
            searchResult = executeSearch(id, name, clubId, pageable, projectJpaEntity.name::contains)
        }
        return searchResult
    }

    private fun executeSearch(
        id: Long?,
        name: String?,
        clubId: Long?,
        pageable: Pageable,
        nameMatcher: (String) -> BooleanExpression,
    ): Page<ProjectJpaEntity> {
        val content =
            jpaQueryFactory
                .select(projectJpaEntity)
                .from(projectJpaEntity)
                .leftJoin(projectJpaEntity.ownerClub)
                .fetchJoin()
                .where(
                    id?.let { projectJpaEntity.id.eq(it) },
                    name?.let { nameMatcher(it) },
                    clubId?.let { projectJpaEntity.ownerClub.id.eq(it) },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(projectJpaEntity.count())
                .from(projectJpaEntity)
                .where(
                    id?.let { projectJpaEntity.id.eq(it) },
                    name?.let { nameMatcher(it) },
                    clubId?.let { projectJpaEntity.ownerClub.id.eq(it) },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
