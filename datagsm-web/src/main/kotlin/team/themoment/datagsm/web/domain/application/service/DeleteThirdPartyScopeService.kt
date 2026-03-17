package team.themoment.datagsm.web.domain.application.service

interface DeleteThirdPartyScopeService {
    fun execute(
        applicationId: String,
        scopeId: Long,
    )
}
