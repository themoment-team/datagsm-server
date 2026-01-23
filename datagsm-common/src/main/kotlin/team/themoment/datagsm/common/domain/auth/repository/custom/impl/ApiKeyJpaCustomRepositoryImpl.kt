package team.themoment.datagsm.common.domain.auth.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import io.github.snowykte0426.querydsl.mysql.json.jpa.JPAJsonFunctions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.auth.entity.ApiKey
import team.themoment.datagsm.common.domain.auth.entity.QApiKey.Companion.apiKey
import team.themoment.datagsm.common.domain.auth.repository.custom.ApiKeyJpaCustomRepository
import team.themoment.datagsm.common.global.data.ApiKeyEnvironment
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
        val results =
            jpaQueryFactory
                .selectFrom(apiKey)
                .leftJoin(apiKey.account)
                .fetchJoin()
                .where(
                    id?.let { apiKey.id.eq(it) },
                    accountId?.let { apiKey.account.id.eq(it) },
                    scope?.let { scopeValue ->
                        JPAJsonFunctions.jsonContainsString(
                            apiKey.scopes,
                            scopeValue,
                        )
                    },
                    isExpired?.let {
                        if (it) apiKey.expiresAt.before(now) else apiKey.expiresAt.after(now)
                    },
                    isRenewable?.let {
                        val renewalCutoff = now.minusDays(apiKeyEnvironment.renewalPeriodDays)
                        if (it) apiKey.expiresAt.after(renewalCutoff) else apiKey.expiresAt.before(renewalCutoff)
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
                    scope?.let { scopeValue ->
                        JPAJsonFunctions.jsonContainsString(
                            apiKey.scopes,
                            scopeValue,
                        )
                    },
                    isExpired?.let {
                        if (it) apiKey.expiresAt.before(now) else apiKey.expiresAt.after(now)
                    },
                    isRenewable?.let {
                        val renewalCutoff = now.minusDays(apiKeyEnvironment.renewalPeriodDays)
                        if (it) apiKey.expiresAt.after(renewalCutoff) else apiKey.expiresAt.before(renewalCutoff)
                    },
                )

        return PageableExecutionUtils.getPage(results, pageable) { countQuery.fetchOne() ?: 0L }
    }
}
