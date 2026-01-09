package team.themoment.datagsm.web.domain.client.service

interface GetAvailableOauthScopesService {
    fun execute(): Set<String>
}
