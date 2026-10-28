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
                // handleUnexpectedToken은 등록된 문제 처리기가 임의 값을 돌려줄 수 있어, 항상 예외를 던지는 쪽을 쓴다.
                ctxt.reportInputMismatch(this, "scope must be a space-delimited string or an array of strings")
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
