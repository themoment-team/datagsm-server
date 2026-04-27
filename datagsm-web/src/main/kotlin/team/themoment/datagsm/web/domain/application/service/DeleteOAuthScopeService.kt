package team.themoment.datagsm.web.domain.application.service

interface DeleteOAuthScopeService {
    fun execute(
        applicationId: String,
        scopeId: Long,
    )
}
