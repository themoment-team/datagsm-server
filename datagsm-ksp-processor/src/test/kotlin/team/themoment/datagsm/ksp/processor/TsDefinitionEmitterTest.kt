package team.themoment.datagsm.ksp.processor

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class TsDefinitionEmitterTest :
    BehaviorSpec({

        // A representative model exercising every branch the emitter handles:
        // enum, flattened DTO reference, array-of-DTO, nullable array element, Map -> Record,
        // optional (nullable) property, and a generic interface with a type-parameter reference.
        fun sampleModel() =
            TsModel(
                enums =
                    listOf(
                        TsEnum("ClubType", listOf("MAJOR_CLUB", "AUTONOMOUS_CLUB")),
                    ),
                interfaces =
                    listOf(
                        TsInterface(
                            name = "ClubResDto",
                            typeParameters = emptyList(),
                            properties =
                                listOf(
                                    TsProperty("id", "number", optional = false),
                                    TsProperty("name", "string", optional = false),
                                    TsProperty("type", "ClubType", optional = false, references = setOf("ClubType")),
                                    TsProperty("members", "ClubMemberDto[]", optional = false, references = setOf("ClubMemberDto")),
                                    TsProperty("tags", "(string | null)[]", optional = false),
                                    TsProperty("meta", "Record<string, number>", optional = false),
                                    TsProperty("description", "string", optional = true),
                                ),
                        ),
                        TsInterface(
                            name = "ClubMemberDto",
                            typeParameters = emptyList(),
                            properties =
                                listOf(
                                    TsProperty("studentId", "number", optional = false),
                                    TsProperty("joinedAt", "string", optional = false),
                                ),
                        ),
                        TsInterface(
                            name = "PageResDto",
                            typeParameters = listOf("T"),
                            properties =
                                listOf(
                                    TsProperty("content", "T[]", optional = false, references = setOf("T")),
                                    TsProperty("total", "number", optional = false),
                                ),
                        ),
                    ),
            )

        Given("enum, 참조, 배열, Map, 제네릭을 모두 포함한 유효한 TsModel이 주어졌을 때") {
            When("TypeScript로 렌더링하면") {
                val rendered = TsDefinitionEmitter.render(sampleModel())

                Then("출력이 골든 index.d.ts와 바이트 단위로 일치한다") {
                    val golden =
                        this::class.java
                            .getResource("/golden/index.d.ts")!!
                            .readText()
                            .replace("\r\n", "\n")
                    rendered shouldBe golden
                }
            }

            When("검증하면") {
                val errors = TsDefinitionEmitter.validate(sampleModel())

                Then("검증 오류가 없다") {
                    errors shouldBe emptyList()
                }
            }
        }

        Given("내보내지지 않은 타입을 프로퍼티가 참조하는 모델이 주어졌을 때") {
            val model =
                TsModel(
                    enums = emptyList(),
                    interfaces =
                        listOf(
                            TsInterface(
                                name = "ClubResDto",
                                typeParameters = emptyList(),
                                properties =
                                    listOf(
                                        TsProperty("owner", "TeacherDto", optional = false, references = setOf("TeacherDto")),
                                    ),
                            ),
                        ),
                )

            When("검증하면") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("dangling 참조를 조용히 내보내지 않고 오류로 보고한다") {
                    errors shouldContainExactlyInAnyOrder
                        listOf("Type 'TeacherDto' referenced by ClubResDto.owner is not exported (missing @KmpExport?)")
                }
            }
        }

        Given("두 타입이 같은 이름으로 평탄화된 모델이 주어졌을 때") {
            val model =
                TsModel(
                    enums = listOf(TsEnum("Status", listOf("OK"))),
                    interfaces =
                        listOf(
                            TsInterface("Status", emptyList(), emptyList()),
                        ),
                )

            When("검증하면") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("중복된 이름을 보고한다") {
                    errors shouldContainExactlyInAnyOrder
                        listOf("Duplicate type names after flattening Status")
                }
            }
        }

        Given("프로퍼티가 자신의 타입 파라미터를 참조하는 제네릭 인터페이스가 주어졌을 때") {
            val model =
                TsModel(
                    enums = emptyList(),
                    interfaces =
                        listOf(
                            TsInterface(
                                name = "PageResDto",
                                typeParameters = listOf("T"),
                                properties = listOf(TsProperty("content", "T[]", optional = false, references = setOf("T"))),
                            ),
                        ),
                )

            When("검증하면") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("타입 파라미터가 dangling 참조가 아닌 가시적인 것으로 처리된다") {
                    errors shouldBe emptyList()
                }
            }
        }
    })
