package team.themoment.datagsm.oauth.authorization.global.util

import java.security.SecureRandom
import kotlin.math.pow

object EmailCodeGenerator {
    private val secureRandom = SecureRandom()
    private const val EMAIL_CODE_LENGTH = 8
    private val EMAIL_RANDOM_MAX = 10.0.pow(EMAIL_CODE_LENGTH).toInt()

    fun generate(): String = secureRandom.nextInt(0, EMAIL_RANDOM_MAX).toString().padStart(EMAIL_CODE_LENGTH, '0')
}
