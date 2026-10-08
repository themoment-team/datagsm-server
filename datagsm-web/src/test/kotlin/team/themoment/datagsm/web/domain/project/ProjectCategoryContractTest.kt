package team.themoment.datagsm.web.domain.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectCategory
import team.themoment.datagsm.common.domain.project.entity.constant.ProjectStatus
import team.themoment.datagsm.common.domain.project.resolveCategoryForUpdate

class ProjectCategoryContractTest :
    DescribeSpec({

        val objectMapper = ObjectMapper().registerKotlinModule()

        describe("프로젝트 카테고리 API 계약은") {

            it("요청 JSON의 category가 ProjectReqDto로 역직렬화되어야 한다") {
                val json =
                    """
                    {"name":"p","description":"d","startYear":2024,"clubId":null,
                     "participantIds":[],"category":"IDEA_FESTIVAL"}
                    """.trimIndent()

                val dto = objectMapper.readValue(json, ProjectReqDto::class.java)

                dto.category shouldBe ProjectCategory.IDEA_FESTIVAL
            }

            it("요청 JSON에 category가 없으면 ApplyProjectReqDto의 category는 null이어야 한다") {
                val json = """{"name":"p","description":"d","startYear":2024}"""

                val dto = objectMapper.readValue(json, ApplyProjectReqDto::class.java)

                dto.category shouldBe null
            }

            it("응답 DTO의 category가 JSON으로 직렬화되어야 한다") {
                val resDto =
                    ProjectResDto(
                        id = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = ProjectStatus.ACTIVE,
                        category = ProjectCategory.PERSONAL,
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(resDto) shouldContain "\"category\":\"PERSONAL\""
            }

            it("웹훅 payload에 category가 직렬화되어야 한다") {
                val eventObject =
                    ProjectEventObject(
                        projectId = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = "ACTIVE",
                        category = ProjectCategory.TEAM.name,
                        deploymentUrl = null,
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(eventObject) shouldContain "\"category\":\"TEAM\""
            }
        }

        describe("resolveCategoryForUpdate 함수는") {

            context("요청 category가 null일 때") {
                it("현재 category를 유지해야 한다") {
                    val result = resolveCategoryForUpdate(null, ProjectCategory.CLUB)

                    result shouldBe ProjectCategory.CLUB
                }
            }

            context("요청 category가 주어질 때") {
                it("요청 category로 변경해야 한다") {
                    val result = resolveCategoryForUpdate(ProjectCategory.TEAM, ProjectCategory.CLUB)

                    result shouldBe ProjectCategory.TEAM
                }
            }
        }
    })
