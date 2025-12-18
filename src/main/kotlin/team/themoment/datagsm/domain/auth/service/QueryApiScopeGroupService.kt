package team.themoment.datagsm.domain.auth.service

import team.themoment.datagsm.domain.auth.dto.response.ApiScopeResDto

interface QueryApiScopeGroupService {
    fun execute(scopeName: String): ApiScopeResDto
}