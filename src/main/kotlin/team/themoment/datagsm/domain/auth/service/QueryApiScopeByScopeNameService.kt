package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeByScopeNameService {
    fun execute(scopeName: String): ApiScopeResDto
}
