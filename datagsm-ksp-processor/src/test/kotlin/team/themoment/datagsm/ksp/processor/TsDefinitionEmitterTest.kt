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

        Given("a valid TsModel covering enums, references, arrays, maps and generics") {
            When("rendering it to TypeScript") {
                val rendered = TsDefinitionEmitter.render(sampleModel())

                Then("the output matches the golden index.d.ts byte-for-byte") {
                    val golden =
                        this::class.java
                            .getResource("/golden/index.d.ts")!!
                            .readText()
                            .replace("\r\n", "\n")
                    rendered shouldBe golden
                }
            }

            When("validating it") {
                val errors = TsDefinitionEmitter.validate(sampleModel())

                Then("there are no validation errors") {
                    errors shouldBe emptyList()
                }
            }
        }

        Given("a model whose property references a type that was never exported") {
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

            When("validating it") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("it reports the dangling reference instead of emitting it silently") {
                    errors shouldContainExactlyInAnyOrder
                        listOf("Type 'TeacherDto' referenced by ClubResDto.owner is not exported (missing @KmpExport?)")
                }
            }
        }

        Given("a model with two types flattened to the same name") {
            val model =
                TsModel(
                    enums = listOf(TsEnum("Status", listOf("OK"))),
                    interfaces =
                        listOf(
                            TsInterface("Status", emptyList(), emptyList()),
                        ),
                )

            When("validating it") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("it reports the duplicate name") {
                    errors shouldContainExactlyInAnyOrder
                        listOf("Duplicate type names after flattening Status")
                }
            }
        }

        Given("a generic interface whose property references its own type parameter") {
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

            When("validating it") {
                val errors = TsDefinitionEmitter.validate(model)

                Then("the type parameter is treated as visible, not a dangling reference") {
                    errors shouldBe emptyList()
                }
            }
        }
    })
