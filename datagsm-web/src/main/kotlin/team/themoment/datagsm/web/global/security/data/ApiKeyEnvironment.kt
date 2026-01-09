package team.themoment.datagsm.web.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.security.api-key")
class ApiKeyEnvironment {
    var expirationDays: Long = 30
    var renewalPeriodDays: Long = 15
    var adminExpirationDays: Long = 365
    var adminRenewalPeriodDays: Long = 30
    var rateLimit: RateLimit = RateLimit()

    class RateLimit {
        var enabled: Boolean = true
        var defaultCapacity: Long = 100
        var defaultRefillTokens: Long = 100
        var defaultRefillDurationSeconds: Long = 60
    }
}
