package team.themoment.datagsm.oauth.authorization.global.util

import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import java.security.MessageDigest
import java.util.Base64

object PkceVerifier {
    fun verify(
        codeChallenge: String,
        codeChallengeMethod: PkceChallengeMethod,
        codeVerifier: String,
    ): Boolean =
        when (codeChallengeMethod) {
            PkceChallengeMethod.S256 -> {
                val hash =
                    MessageDigest
                        .getInstance("SHA-256")
                        .digest(codeVerifier.toByteArray())
                val encoded =
                    Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(hash)
                MessageDigest.isEqual(codeChallenge.toByteArray(), encoded.toByteArray())
            }
            PkceChallengeMethod.PLAIN -> {
                MessageDigest.isEqual(codeChallenge.toByteArray(), codeVerifier.toByteArray())
            }
        }
}
