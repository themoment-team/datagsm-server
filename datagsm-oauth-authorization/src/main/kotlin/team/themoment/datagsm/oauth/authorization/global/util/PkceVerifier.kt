package team.themoment.datagsm.oauth.authorization.global.util

import java.security.MessageDigest
import java.util.Base64

object PkceVerifier {
    fun verify(
        codeChallenge: String,
        codeChallengeMethod: String,
        codeVerifier: String,
    ): Boolean {
        return when (codeChallengeMethod) {
            "S256" -> {
                val hash = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.toByteArray())
                val encoded = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash)
                MessageDigest.isEqual(codeChallenge.toByteArray(), encoded.toByteArray())
            }
            "plain" -> {
                MessageDigest.isEqual(codeChallenge.toByteArray(), codeVerifier.toByteArray())
            }
            else -> false
        }
    }
}
