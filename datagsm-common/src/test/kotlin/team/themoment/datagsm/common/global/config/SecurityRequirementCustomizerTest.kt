package team.themoment.datagsm.common.global.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

class SecurityRequirementCustomizerTest :
    DescribeSpec({
        val securityScheme = SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
        val customizer =
            SecurityRequirementCustomizer(
                schemeName = "bearerAuth",
                securityScheme = securityScheme,
                isPublicPath = { it == "/v1/health" },
            )

        fun openApi(): OpenAPI =
            OpenAPI().paths(
                Paths()
                    .addPathItem("/v1/health", PathItem().get(Operation()))
                    .addPathItem("/v1/accounts/my", PathItem().get(Operation()).delete(Operation())),
            )

        describe("SecurityRequirementCustomizer 클래스의") {
            describe("customise 메서드는") {
                context("문서에 보안 스킴이 없으면") {
                    it("스킴을 등록하고 전역 security requirement로 건다") {
                        // Given
                        val openApi = openApi()

                        // When
                        customizer.customise(openApi)

                        // Then
                        openApi.components.securitySchemes["bearerAuth"] shouldBeSameInstanceAs securityScheme
                        openApi.security shouldBe listOf(SecurityRequirement().addList("bearerAuth"))
                    }
                }

                context("공개 경로와 인증이 필요한 경로가 함께 있으면") {
                    it("공개 경로의 operation만 빈 security로 전역 requirement를 해제한다") {
                        // Given
                        val openApi = openApi()

                        // When
                        customizer.customise(openApi)

                        // Then
                        Json.mapper().writeValueAsString(openApi.paths["/v1/health"]) shouldContain "\"security\":[]"
                        openApi.paths["/v1/accounts/my"]
                            ?.get
                            ?.security
                            .shouldBeNull()
                        openApi.paths["/v1/accounts/my"]
                            ?.delete
                            ?.security
                            .shouldBeNull()
                    }
                }
            }
        }
    })
