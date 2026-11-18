package team.themoment.datagsm.common.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.MediaType
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import team.themoment.sdk.config.ResponseProperties

// the-sdk의 OperationCustomizer는 not-wrapping-urls를 보지 않고 모든 200 스키마를 감싼다.
// Operation 단계에서는 sdk보다 먼저 실행되므로, 모든 Operation이 만들어진 뒤인 OpenAPI 단계에서 되돌린다.
@Component
class NotWrappingResponseSchemaCustomizer(
    private val responseProperties: ResponseProperties,
) : GlobalOpenApiCustomizer {
    private val pathMatcher = AntPathMatcher()

    override fun customise(openApi: OpenAPI) {
        openApi.paths
            ?.filterKeys { path -> isNotWrappingPath(path) }
            ?.values
            ?.flatMap { it.readOperations() }
            ?.mapNotNull { it.responses?.get(WRAPPED_RESPONSE_CODE)?.content }
            ?.flatMap { it.values }
            ?.forEach { unwrap(it) }
    }

    private fun isNotWrappingPath(path: String): Boolean = responseProperties.notWrappingUrls.any { pathMatcher.match(it, path) }

    private fun unwrap(mediaType: MediaType) {
        val properties = mediaType.schema?.takeIf { it.`$ref` == null }?.properties ?: return
        if (properties.keys == WRAPPER_FIELDS) {
            mediaType.schema = properties[DATA_FIELD]
        }
    }

    companion object {
        private const val WRAPPED_RESPONSE_CODE = "200"
        private const val DATA_FIELD = "data"
        private val WRAPPER_FIELDS = setOf("status", "code", "message", DATA_FIELD)
    }
}
