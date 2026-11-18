package team.themoment.datagsm.common.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.stereotype.Component

// 컨트롤러는 오류 응답을 content = [Content()]로 적어 반환 타입 스키마가 추론되지 않게 막는다.
// 실제 오류 본문은 예외 핸들러와 보안 필터가 내려주는 CommonApiResponse이므로, 비어 있는 오류 응답에만 그 형태를 채운다.
@Component
class ErrorResponseSchemaCustomizer : GlobalOpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        val errorResponses =
            openApi.paths
                ?.values
                ?.flatMap { it.readOperations() }
                ?.flatMap { operation -> operation.responses?.entries.orEmpty() }
                ?.filter { (code, response) -> isError(code) && response.hasNoContent() }
                ?.map { it.value }
                .orEmpty()
        if (errorResponses.isEmpty()) return

        val components = openApi.components ?: Components().also { openApi.components = it }
        components.addSchemas(SCHEMA_NAME, errorSchema())
        errorResponses.forEach { response ->
            response.content =
                Content().addMediaType(
                    JSON_MEDIA_TYPE,
                    MediaType().schema(Schema<Any>().`$ref`(SCHEMA_REF)),
                )
        }
    }

    private fun isError(code: String): Boolean = code.toIntOrNull()?.let { it >= ERROR_STATUS_FLOOR } ?: false

    private fun ApiResponse.hasNoContent(): Boolean = `$ref` == null && content.isNullOrEmpty()

    private fun errorSchema(): Schema<*> =
        ObjectSchema()
            .description("오류 응답")
            .addProperty("status", StringSchema().description("HTTP 상태").example("400 BAD_REQUEST"))
            .addProperty("code", IntegerSchema().description("HTTP 상태 코드").example(400))
            .addProperty("message", StringSchema().description("오류 메시지").example("잘못된 요청입니다."))
            .required(listOf("status", "code", "message"))

    companion object {
        private const val ERROR_STATUS_FLOOR = 400
        private const val SCHEMA_NAME = "ErrorResponse"
        private const val SCHEMA_REF = "#/components/schemas/$SCHEMA_NAME"
        private const val JSON_MEDIA_TYPE = "application/json"
    }
}
