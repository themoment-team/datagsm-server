package team.themoment.datagsm.oauth.authorization.domain.oauth.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.oauth.dto.response.JwkSetResDto
import team.themoment.datagsm.common.domain.oauth.dto.response.RsaPublicJwkDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryJwkSetService
import team.themoment.datagsm.oauth.authorization.global.security.jwt.JwtProvider
import java.security.interfaces.RSAPublicKey

@Service
class QueryJwkSetServiceImpl(
    private val jwtProvider: JwtProvider,
) : QueryJwkSetService {
    override fun execute(): JwkSetResDto {
        val rsaPublicKey = jwtProvider.getPublicKey() as RSAPublicKey
        val jwk = RsaPublicJwkDto.from(publicKey = rsaPublicKey, kid = jwtProvider.getKeyId())
        return JwkSetResDto(keys = listOf(jwk))
    }
}
