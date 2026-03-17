package team.themoment.datagsm.web.domain.auth.service

import java.time.LocalDateTime

interface ExpireApiKeyService {
    fun execute(cutoffDate: LocalDateTime): Long
}
