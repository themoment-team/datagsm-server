package team.themoment.datagsm.web.domain.auth.service

import team.themoment.datagsm.common.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeByScopeNameService {
    fun execute(scopeName: String): ApiScopeResDto
}
