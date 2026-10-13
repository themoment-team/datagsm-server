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
import team.themoment.datagsm.common.domain.project.resolveCategoriesForUpdate
import team.themoment.datagsm.common.domain.project.toOrderedSet

class ProjectCategoryContractTest :
    DescribeSpec({

        val objectMapper = ObjectMapper().registerKotlinModule()

        describe("프로젝트 카테고리 API 계약은") {

            it("요청 JSON의 categories가 ProjectReqDto로 역직렬화되어야 한다") {
                val json =
                    """
                    {"name":"p","description":"d","startYear":2024,"clubId":null,
                     "participantIds":[],"categories":["IDEA_FESTIVAL","TOY_PROJECT"]}
                    """.trimIndent()

                val dto = objectMapper.readValue(json, ProjectReqDto::class.java)

                dto.categories shouldBe setOf(ProjectCategory.IDEA_FESTIVAL, ProjectCategory.TOY_PROJECT)
            }

            it("요청 JSON에 categories가 없으면 ApplyProjectReqDto의 categories는 null이어야 한다") {
                val json = """{"name":"p","description":"d","startYear":2024}"""

                val dto = objectMapper.readValue(json, ApplyProjectReqDto::class.java)

                dto.categories shouldBe null
            }

            it("응답 DTO의 categories가 JSON으로 직렬화되어야 한다") {
                val resDto =
                    ProjectResDto(
                        id = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = ProjectStatus.ACTIVE,
                        categories = setOf(ProjectCategory.TOY_PROJECT, ProjectCategory.TEAM),
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(resDto) shouldContain "\"categories\":[\"TOY_PROJECT\",\"TEAM\"]"
            }

            it("웹훅 payload에 categories가 직렬화되어야 한다") {
                val eventObject =
                    ProjectEventObject(
                        projectId = 1L,
                        name = "p",
                        description = "d",
                        startYear = 2024,
                        endYear = null,
                        status = "ACTIVE",
                        categories = listOf(ProjectCategory.TEAM.name, ProjectCategory.CLUB.name),
                        deploymentUrl = null,
                        club = null,
                        participants = emptyList(),
                        repositories = emptyList(),
                        techStacks = emptyList(),
                    )

                objectMapper.writeValueAsString(eventObject) shouldContain "\"categories\":[\"TEAM\",\"CLUB\"]"
            }
        }

        describe("resolveCategoriesForUpdate 함수는") {

            context("요청 categories가 null일 때") {
                it("현재 categories를 유지해야 한다") {
                    val result = resolveCategoriesForUpdate(null, setOf(ProjectCategory.CLUB, ProjectCategory.TEAM))

                    result shouldBe setOf(ProjectCategory.CLUB, ProjectCategory.TEAM)
                }
            }

            context("요청 categories가 주어질 때") {
                it("요청 categories로 교체해야 한다") {
                    val result = resolveCategoriesForUpdate(setOf(ProjectCategory.TEAM), setOf(ProjectCategory.CLUB))

                    result shouldBe setOf(ProjectCategory.TEAM)
                }
            }

            context("요청 categories가 빈 집합일 때") {
                it("모든 카테고리를 해제해야 한다") {
                    val result = resolveCategoriesForUpdate(emptySet(), setOf(ProjectCategory.CLUB))

                    result shouldBe emptySet()
                }
            }
        }

        describe("toOrderedSet 함수는") {
            it("입력 순서와 무관하게 enum 선언 순서로 정렬해야 한다") {
                val result = setOf(ProjectCategory.NOT_DEPLOYED, ProjectCategory.TEAM, ProjectCategory.TOY_PROJECT).toOrderedSet()

                result.toList() shouldBe listOf(ProjectCategory.TOY_PROJECT, ProjectCategory.TEAM, ProjectCategory.NOT_DEPLOYED)
            }
        }
    })
