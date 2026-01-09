package team.themoment.datagsm.common.domain.auth.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.entity.QApiKey.Companion.apiKey
import team.themoment.datagsm.common.domain.auth.repository.custom.ApiKeyJpaCustomRepository
import team.themoment.datagsm.common.global.security.data.ApiKeyEnvironment
import java.time.LocalDateTime

@Repository
class ApiKeyJpaCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
    private val apiKeyEnvironment: ApiKeyEnvironment,
) : ApiKeyJpaCustomRepository {
    override fun searchApiKeyWithPaging(
        id: Long?,
        accountId: Long?,
        scope: String?,
        isExpired: Boolean?,
        isRenewable: Boolean?,
        pageable: Pageable,
    ): Page<ApiKey> {
        val now = LocalDateTime.now()
        val renewalPeriodDays = apiKeyEnvironment.renewalPeriodDays
        val renewalDeadline = now.minusDays(renewalPeriodDays)

        val content =
            jpaQueryFactory
                .selectFrom(apiKey)
                .leftJoin(apiKey.account)
                .fetchJoin()
                .where(
                    id?.let { apiKey.id.eq(it) },
                    accountId?.let { apiKey.account.id.eq(it) },
                    scope?.let { apiKey._scopes.contains(it) },
                    isExpired?.let {
                        if (it) apiKey.expiresAt.before(now) else apiKey.expiresAt.after(now)
                    },
                    isRenewable?.let {
                        if (it) {
                            apiKey.expiresAt.after(renewalDeadline)
                        } else {
                            apiKey.expiresAt.before(renewalDeadline)
                        }
                    },
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(apiKey.count())
                .from(apiKey)
                .where(
                    id?.let { apiKey.id.eq(it) },
                    accountId?.let { apiKey.account.id.eq(it) },
                    scope?.let { apiKey._scopes.contains(it) },
                    isExpired?.let {
                        if (it) apiKey.expiresAt.before(now) else apiKey.expiresAt.after(now)
                    },
                    isRenewable?.let {
                        if (it) {
                            apiKey.expiresAt.after(renewalDeadline)
                        } else {
                            apiKey.expiresAt.before(renewalDeadline)
                        }
                    },
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
