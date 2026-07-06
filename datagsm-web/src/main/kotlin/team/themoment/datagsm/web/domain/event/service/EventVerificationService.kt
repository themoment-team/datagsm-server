package team.themoment.datagsm.web.domain.event.service

interface EventVerificationService {
    fun verifyAsync(eventId: Long)
}
