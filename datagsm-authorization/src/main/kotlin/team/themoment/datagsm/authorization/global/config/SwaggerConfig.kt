package team.themoment.datagsm.authorization.global.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.authorization.global.common.response.dto.response.CommonApiResponse

@OpenAPIDefinition(
    info =
        Info(
            title = "DataGSM",
            description = "GSM Open API 서비스",
            version = "v1",
        ),
)
@Configuration
class SwaggerConfig {
    @Bean
    fun api(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("DataGSM API")
            .pathsToMatch("/v1/**")
            .addOperationCustomizer(customOperationCustomizer())
            .build()

    private fun customOperationCustomizer(): OperationCustomizer =
        OperationCustomizer { operation, handlerMethod ->
            val returnType = handlerMethod.method.returnType
            val hideDataField = CommonApiResponse::class.java.isAssignableFrom(returnType)
            addResponseBodyWrapperSchemaExample(operation, hideDataField)

            operation
        }

    private fun addResponseBodyWrapperSchemaExample(
        operation: Operation,
        hideDataField: Boolean,
    ) {
        operation.responses["200"]?.content?.let { content ->
            content.forEach { (_, mediaType) ->
                val originalSchema = mediaType.schema
                mediaType.schema = wrapSchema(originalSchema, hideDataField)
            }
        }
    }

    private fun wrapSchema(
        originalSchema: Schema<*>?,
        hideDataField: Boolean,
    ): Schema<*> =
        Schema<Any>().apply {
            addProperty("status", Schema<String>().type("string").example("OK"))
            addProperty("code", Schema<Int>().type("integer").example(200))
            addProperty("message", Schema<String>().type("string").example("OK"))
            if (!hideDataField) {
                addProperty("data", originalSchema)
            }
        }
}
