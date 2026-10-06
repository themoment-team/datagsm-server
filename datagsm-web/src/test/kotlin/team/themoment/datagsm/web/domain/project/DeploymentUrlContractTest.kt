package team.themoment.datagsm.web.domain.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import jakarta.validation.Validation
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus

class DeploymentUrlContractTest :
    DescribeSpec({

        val objectMapper = ObjectMapper().registerKotlinModule()
        val validator = Validation.buildDefaultValidatorFactory().validator

        describe("배포 URL API 계약은") {

            it("요청 JSON의 deploymentUrl이 ProjectReqDto로 역직렬화되어야 한다") {
                val json =
                    """
                    {"name":"p","description":"d","startYear":2024,"clubId":null,
                     "participantIds":[],"deploymentUrl":"https://datagsm.kr"}
                    """.trimIndent()

                val dto = objectMapper.readValue(json, ProjectReqDto::class.java)

                dto.deploymentUrl shouldBe "https://datagsm.kr"
            }

            it("요청 JSON의 deploymentUrl이 ApplyProjectReqDto로 역직렬화되어야 한다") {
                val json =
                    """
                    {"name":"p","description":"d","startYear":2024,
                     "deploymentUrl":"https://datagsm.kr"}
                    """.trimIndent()

                val dto = objectMapper.readValue(json, ApplyProjectReqDto::class.java)

                dto.deploymentUrl shouldBe "https://datagsm.kr"
            }

            it("응답 DTO의 deploymentUrl이 JSON으로 직렬화되어야 한다") {
                val resDto =
                    ProjectResDto(
                        id = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = ProjectStatus.ACTIVE,
                        iconUrl = null,
                        deploymentUrl = "https://datagsm.kr",
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(resDto) shouldContain "\"deploymentUrl\":\"https://datagsm.kr\""
            }

            it("웹훅 payload는 snake_case deployment_url로 직렬화되어야 한다") {
                val eventObject =
                    ProjectEventObject(
                        projectId = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = "ACTIVE",
                        deploymentUrl = "https://datagsm.kr",
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(eventObject) shouldContain "\"deployment_url\":\"https://datagsm.kr\""
            }

            it("http/https가 아닌 배포 URL은 검증에서 걸러져야 한다") {
                val dto =
                    ProjectReqDto(
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        clubId = null,
                        participantIds = emptyList(),
                        deploymentUrl = "javascript:alert(1)",
                    )

                val violations = validator.validate(dto)

                violations.size shouldBe 1
                violations.first().propertyPath.toString() shouldBe "deploymentUrl"
            }

            it("배포 URL이 null이면 검증을 통과해야 한다") {
                val dto =
                    ProjectReqDto(
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        clubId = null,
                        participantIds = emptyList(),
                        deploymentUrl = null,
                    )

                validator.validate(dto).isEmpty() shouldBe true
            }

            it("300자를 초과하는 배포 URL은 검증에서 걸러져야 한다") {
                val dto =
                    ProjectReqDto(
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        clubId = null,
                        participantIds = emptyList(),
                        deploymentUrl = "https://datagsm.kr/" + "a".repeat(300),
                    )

                validator.validate(dto).any { it.propertyPath.toString() == "deploymentUrl" } shouldBe true
            }
        }
    })
