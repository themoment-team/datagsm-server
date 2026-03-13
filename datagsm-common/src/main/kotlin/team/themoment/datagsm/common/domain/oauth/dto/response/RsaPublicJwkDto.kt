package team.themoment.datagsm.common.domain.oauth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.security.interfaces.RSAPublicKey
import java.util.Base64

data class RsaPublicJwkDto(
    @field:Schema(description = "키 타입", example = "RSA")
    val kty: String = "RSA",
    @field:Schema(description = "키 ID", example = "my-key-id")
    val kid: String,
    @field:Schema(description = "RSA 공개키 모듈러스 (Base64url)")
    val n: String,
    @field:Schema(description = "RSA 공개키 지수 (Base64url)", example = "AQAB")
    val e: String,
    @field:Schema(description = "키 용도", example = "sig", nullable = true)
    val use: String? = null,
    @field:Schema(description = "알고리즘", example = "RS256", nullable = true)
    val alg: String? = null,
) {
    companion object {
        fun from(
            publicKey: RSAPublicKey,
            kid: String,
            use: String? = null,
            alg: String? = null,
        ): RsaPublicJwkDto {
            val encoder = Base64.getUrlEncoder().withoutPadding()
            return RsaPublicJwkDto(
                kid = kid,
                n = encoder.encodeToString(publicKey.modulus.toByteArray().stripLeadingZero()),
                e = encoder.encodeToString(publicKey.publicExponent.toByteArray().stripLeadingZero()),
                use = use,
                alg = alg,
            )
        }

        // BigInteger.toByteArray()는 부호 비트 때문에 앞에 0x00이 붙을 수 있음 → JWK 스펙상 제거 필요
        private fun ByteArray.stripLeadingZero(): ByteArray =
            if (this.isNotEmpty() && this[0] == 0.toByte()) this.copyOfRange(1, this.size) else this
    }
}
