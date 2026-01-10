package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeByScopeNameService {
    fun execute(scopeName: String): ApiScopeResDto
}
