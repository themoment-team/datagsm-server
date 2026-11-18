package team.themoment.datagsm.common.global.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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

class ErrorResponseSchemaCustomizerTest :
    DescribeSpec({
        val customizer = ErrorResponseSchemaCustomizer()

        fun openApiOf(responses: ApiResponses): OpenAPI =
            OpenAPI().paths(Paths().addPathItem("/v1/sample", PathItem().get(Operation().responses(responses))))

        fun responseOf(
            openApi: OpenAPI,
            code: String,
        ): ApiResponse? =
            openApi.paths["/v1/sample"]
                ?.get
                ?.responses
                ?.get(code)

        describe("ErrorResponseSchemaCustomizer 클래스의") {
            describe("customise 메서드는") {
                context("본문이 비어 있는 오류 응답이 있으면") {
                    it("CommonApiResponse 오류 형태의 스키마를 채운다") {
                        // Given
                        val openApi =
                            openApiOf(
                                ApiResponses()
                                    .addApiResponse("403", ApiResponse().description("권한 부족").content(Content()))
                                    .addApiResponse("404", ApiResponse().description("대상 없음")),
                            )

                        // When
                        customizer.customise(openApi)

                        // Then
                        listOf("403", "404").forEach { code ->
                            responseOf(openApi, code)
                                ?.content
                                ?.get("application/json")
                                ?.schema
                                ?.`$ref` shouldBe "#/components/schemas/ErrorResponse"
                        }
                        val errorSchema = openApi.components.schemas["ErrorResponse"].shouldNotBeNull()
                        errorSchema.properties.keys shouldBe setOf("status", "code", "message")
                    }
                }

                context("오류 응답에 본문이 이미 명시되어 있으면") {
                    it("명시된 스키마를 그대로 둔다") {
                        // Given
                        val explicit = Schema<Any>().`$ref`("#/components/schemas/OAuthErrorResDto")
                        val openApi =
                            openApiOf(
                                ApiResponses().addApiResponse(
                                    "400",
                                    ApiResponse().content(
                                        Content().addMediaType("application/json", MediaType().schema(explicit)),
                                    ),
                                ),
                            )

                        // When
                        customizer.customise(openApi)

                        // Then
                        responseOf(openApi, "400")?.content?.get("application/json")?.schema shouldBeSameInstanceAs explicit
                    }
                }

                context("오류가 아닌 응답이 본문 없이 정의되어 있으면") {
                    it("본문을 채우지 않는다") {
                        // Given
                        val openApi =
                            openApiOf(
                                ApiResponses()
                                    .addApiResponse("204", ApiResponse().description("삭제 완료"))
                                    .addApiResponse("302", ApiResponse().description("리다이렉트")),
                            )

                        // When
                        customizer.customise(openApi)

                        // Then
                        responseOf(openApi, "204")?.content.shouldBeNull()
                        responseOf(openApi, "302")?.content.shouldBeNull()
                        openApi.components.shouldBeNull()
                    }
                }
            }
        }
    })
