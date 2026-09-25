package team.themoment.datagsm.web.global.security.service

interface IconUploadRateLimitService {
    fun ensureNotExceeded(accountEmail: String)
}
