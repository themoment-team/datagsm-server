package team.themoment.datagsm.common.domain.oauth.dto.request

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

/**
 * OAuth 표준의 scope는 공백으로 구분한 문자열이지만, 기존 클라이언트는 JSON 배열로 보내므로 둘 다 받는다.
 * 빈 값은 scope를 생략한 것으로 보고 null로 둔다. 빈 집합이 되면 원래 부여된 범위로 돌아가지 못하고 id_token도 빠진다.
 */
class OauthScopeSetDeserializer : ValueDeserializer<Set<String>?>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Set<String>? =
        when (p.currentToken()) {
            JsonToken.VALUE_STRING -> {
                splitScopes(p.string)
            }

            JsonToken.START_ARRAY -> {
                generateSequence { p.nextToken() }
                    .takeWhile { it != JsonToken.END_ARRAY }
                    .map { p.valueAsString }
                    .filterNotNull()
                    .filter { it.isNotBlank() }
                    .toSet()
                    .ifEmpty { null }
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                ctxt.handleUnexpectedToken(Set::class.java, p) as Set<String>
            }
        }

    companion object {
        private val WHITESPACE = "\\s+".toRegex()

        fun splitScopes(value: String): Set<String>? =
            value
                .split(WHITESPACE)
                .filter { it.isNotBlank() }
                .toSet()
                .ifEmpty { null }
    }
}
