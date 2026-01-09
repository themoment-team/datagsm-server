package team.themoment.datagsm.resource.domain.auth.service

import team.themoment.datagsm.common.dto.auth.response.ApiScopeResDto

interface QueryApiScopeByScopeNameService {
    fun execute(scopeName: String): ApiScopeResDto
}
