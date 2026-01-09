package team.themoment.datagsm.resource.domain.auth.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.account.ApiScope
import team.themoment.datagsm.common.dto.auth.response.ApiScopeResDto
import team.themoment.datagsm.resource.domain.auth.service.QueryApiScopeByScopeNameService
import team.themoment.sdk.exception.ExpectedException

@Service
class QueryApiScopeByScopeNameServiceImpl : QueryApiScopeByScopeNameService {
    override fun execute(scopeName: String): ApiScopeResDto {
        val apiScope =
            ApiScope.fromString(scopeName)
                ?: throw ExpectedException("해당 권한 범위 $scopeName 는 존재하지 않습니다.", HttpStatus.NOT_FOUND)

        return ApiScopeResDto(
            scope = apiScope.scope,
            description = apiScope.description,
        )
    }
}
