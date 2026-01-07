package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.resource.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeByScopeNameService {
    fun execute(scopeName: String): ApiScopeResDto
}
