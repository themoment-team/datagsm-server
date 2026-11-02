package team.themoment.datagsm.oauth.authorization.global.util

import org.springframework.util.MultiValueMap
import team.themoment.datagsm.common.domain.oauth.dto.request.Oauth2TokenReqDto
import team.themoment.datagsm.common.domain.oauth.dto.request.OauthScopeSetDeserializer
import team.themoment.datagsm.common.domain.oauth.exception.OAuthException
import java.net.URLDecoder
import java.util.Base64

/**
 * 표준 OAuth 라이브러리가 보내는 token 요청(form 인코딩, client_secret_basic)을 기존 JSON 요청과 같은 DTO로 맞춘다.
 */
object Oauth2TokenRequestReader {
    private const val BASIC_PREFIX = "Basic "

    fun fromForm(params: MultiValueMap<String, String>): Oauth2TokenReqDto {
        // RFC 6749 3.2: 같은 파라미터를 두 번 보내면 어느 값을 믿을지 정할 수 없으므로 거절한다.
        params.entries.firstOrNull { it.value.size > 1 }?.let {
            throw OAuthException.InvalidRequest("요청 파라미터는 한 번씩만 보낼 수 있습니다.")
        }
        val values = params.toSingleValueMap()
        val grantType =
            values["grant_type"]?.takeIf { it.isNotBlank() }
                ?: throw OAuthException.InvalidRequest("grant_type은 필수입니다.")

        return Oauth2TokenReqDto(
            grantType = grantType,
            clientId = values["client_id"],
            clientSecret = values["client_secret"],
            code = values["code"],
            redirectUri = values["redirect_uri"],
            refreshToken = values["refresh_token"],
            scope = values["scope"]?.let { OauthScopeSetDeserializer.splitScopes(it) },
            codeVerifier = values["code_verifier"],
        )
    }

    fun applyBasicCredentials(
        reqDto: Oauth2TokenReqDto,
        authorization: String?,
    ): Oauth2TokenReqDto {
        if (authorization == null || !authorization.startsWith(BASIC_PREFIX, ignoreCase = true)) {
            return reqDto
        }
        val (clientId, clientSecret) = decodeBasicCredentials(authorization.substring(BASIC_PREFIX.length))

        // RFC 6749 2.3: 한 요청에 클라이언트 인증 방식을 둘 이상 쓰면 안 된다.
        if (!reqDto.clientSecret.isNullOrBlank()) {
            throw OAuthException.InvalidRequest("클라이언트 인증은 한 가지 방식만 사용할 수 있습니다.")
        }
        if (!reqDto.clientId.isNullOrBlank() && reqDto.clientId != clientId) {
            throw OAuthException.InvalidRequest("client_id가 Authorization 헤더와 일치하지 않습니다.")
        }
        return reqDto.copy(clientId = clientId, clientSecret = clientSecret)
    }

    // RFC 6749 2.3.1: client_id와 client_secret을 각각 form URL 인코딩한 뒤 콜론으로 이어 Base64로 인코딩한다.
    private fun decodeBasicCredentials(encoded: String): Pair<String, String> {
        val decoded =
            runCatching { String(Base64.getDecoder().decode(encoded.trim()), Charsets.UTF_8) }
                .getOrElse { throw OAuthException.InvalidClient("Authorization 헤더의 클라이언트 인증 정보가 올바르지 않습니다.") }
        val separatorIndex = decoded.indexOf(':')
        if (separatorIndex <= 0) {
            throw OAuthException.InvalidClient("Authorization 헤더의 클라이언트 인증 정보가 올바르지 않습니다.")
        }
        return runCatching {
            URLDecoder.decode(decoded.substring(0, separatorIndex), Charsets.UTF_8) to
                URLDecoder.decode(decoded.substring(separatorIndex + 1), Charsets.UTF_8)
        }.getOrElse { throw OAuthException.InvalidClient("Authorization 헤더의 클라이언트 인증 정보가 올바르지 않습니다.") }
    }
}
