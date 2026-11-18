package team.themoment.datagsm.common.global.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springframework.web.method.HandlerMethod
import team.themoment.sdk.config.ResponseProperties
import team.themoment.sdk.config.SwaggerProperties
import team.themoment.sdk.response.CommonApiResponse
import team.themoment.sdk.swagger.SwaggerConfig

private class SampleController {
    fun raw(): ByteArray = ByteArray(0)

    fun common(): CommonApiResponse<Nothing> = CommonApiResponse.success("OK")
}

class NotWrappingResponseSchemaCustomizerTest :
    DescribeSpec({
        val sdkOperationCustomizer = SwaggerConfig(SwaggerProperties()).operationCustomizer()
        val customizer =
            NotWrappingResponseSchemaCustomizer(
                ResponseProperties(
                    notWrappingUrls = listOf("/v3/api-docs/**", "/v1/students/exports/excel", "/v1/oauth/authorize"),
                ),
            )

        fun operationOf(schema: Schema<*>?): Operation =
            Operation().responses(
                ApiResponses().addApiResponse(
                    "200",
                    ApiResponse().content(Content().addMediaType("*/*", MediaType().schema(schema))),
                ),
            )

        fun wrappedBySdk(
            schema: Schema<*>?,
            handler: String = "raw",
        ): Operation =
            sdkOperationCustomizer.customize(
                operationOf(schema),
                HandlerMethod(SampleController(), SampleController::class.java.getMethod(handler)),
            )

        fun openApiOf(
            path: String,
            operation: Operation,
        ): OpenAPI = OpenAPI().paths(Paths().addPathItem(path, PathItem().get(operation)))

        fun successSchema(
            openApi: OpenAPI,
            path: String,
        ): Schema<*>? =
            openApi.paths[path]
                ?.get
                ?.responses
                ?.get("200")
                ?.content
                ?.get("*/*")
                ?.schema

        describe("NotWrappingResponseSchemaCustomizer 클래스의") {
            describe("customise 메서드는") {
                context("sdk가 감싼 200 스키마의 경로가 not-wrapping-urls에 해당하면") {
                    it("래퍼를 벗겨 원래 스키마로 되돌린다") {
                        // Given
                        val original = Schema<Any>().type("string").format("binary")
                        val openApi = openApiOf("/v1/students/exports/excel", wrappedBySdk(original))

                        // When
                        customizer.customise(openApi)

                        // Then
                        successSchema(openApi, "/v1/students/exports/excel") shouldBeSameInstanceAs original
                    }

                    it("원래 스키마가 없던 응답은 스키마 없는 상태로 되돌린다") {
                        // Given
                        val openApi = openApiOf("/v1/students/exports/excel", wrappedBySdk(null))

                        // When
                        customizer.customise(openApi)

                        // Then
                        successSchema(openApi, "/v1/students/exports/excel").shouldBeNull()
                    }
                }

                context("경로가 not-wrapping-urls에 해당하지 않으면") {
                    it("실제 응답도 감싸지므로 래퍼 스키마를 그대로 둔다") {
                        // Given
                        val original = Schema<Any>().`$ref`("#/components/schemas/StudentResDto")
                        val openApi = openApiOf("/v1/students", wrappedBySdk(original))

                        // When
                        customizer.customise(openApi)

                        // Then
                        val schema = successSchema(openApi, "/v1/students")
                        schema?.properties?.keys shouldBe setOf("status", "code", "message", "data")
                        schema?.properties?.get("data") shouldBeSameInstanceAs original
                    }

                    it("패턴의 하위 경로라는 이유만으로 벗기지 않는다") {
                        // Given
                        val original = Schema<Any>().`$ref`("#/components/schemas/OauthConsentResDto")
                        val openApi = openApiOf("/v1/oauth/authorize/consent", wrappedBySdk(original))

                        // When
                        customizer.customise(openApi)

                        // Then
                        successSchema(openApi, "/v1/oauth/authorize/consent")?.properties?.keys shouldBe
                            setOf("status", "code", "message", "data")
                    }
                }

                context("not-wrapping 경로의 핸들러가 CommonApiResponse를 직접 반환하면") {
                    it("data가 없는 래퍼는 실제 응답 모양이므로 그대로 둔다") {
                        // Given
                        val openApi =
                            openApiOf(
                                "/v1/oauth/authorize",
                                wrappedBySdk(Schema<Any>().`$ref`("#/components/schemas/CommonApiResponse"), handler = "common"),
                            )

                        // When
                        customizer.customise(openApi)

                        // Then
                        successSchema(openApi, "/v1/oauth/authorize")?.properties?.keys shouldBe setOf("status", "code", "message")
                    }
                }

                context("not-wrapping 경로의 200 스키마가 래퍼가 아니면") {
                    it("스키마를 건드리지 않는다") {
                        // Given
                        val original = Schema<Any>().`$ref`("#/components/schemas/Oauth2TokenResDto")
                        val openApi = openApiOf("/v1/oauth/authorize", operationOf(original))

                        // When
                        customizer.customise(openApi)

                        // Then
                        successSchema(openApi, "/v1/oauth/authorize") shouldBeSameInstanceAs original
                    }
                }
            }
        }
    })
