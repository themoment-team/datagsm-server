package team.themoment.datagsm.common.domain.event.arch

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File

/**
 * student/club/project 도메인의 쓰기 서비스가 이벤트 발행을 누락한 채 머지되는 것을 막는다.
 * 소스를 직접 스캔하므로 단위 테스트와 달리 특정 클래스의 동작이 아니라 저장소 구조를 검증한다.
 *
 * 발행 계약이 [team.themoment.datagsm.common.domain.event] 에 정의되어 있어 검사도 이 모듈에 두며,
 * 해당 도메인을 가진 모든 모듈을 탐색하므로 새 모듈이 생겨도 자동으로 대상에 포함된다.
 */
class EventDispatchConventionTest :
    DescribeSpec({

        val modules = scanModules(findRepositoryRoot())

        describe("student/club/project 도메인의 쓰기 트랜잭션 서비스는") {

            context("저장소 소스를 스캔할 때") {
                it("readOnly가 아닌 @Transactional 서비스를 찾을 수 있어야 한다") {
                    withClue("스캔 경로가 잘못되면 검사가 아무것도 하지 않은 채 통과한다") {
                        modules.flatMap { it.writeServices }.shouldNotBeEmpty()
                    }
                }
            }

            context("이벤트 발행 여부를 확인할 때") {
                it("$EVENT_MARKER 를 발행하거나 allowlist에 등록되어 있어야 한다") {
                    val missing =
                        modules.flatMap { module ->
                            val allowed = allowedClassNames(module.name)
                            module.writeServices
                                .filter { !it.dispatches && it.className !in allowed }
                                .map { "${module.name}/${it.path}" }
                        }

                    withClue({
                        buildString {
                            appendLine("이벤트 발행이 누락되었습니다.")
                            missing.forEach { appendLine("  $it") }
                            append("발행을 추가하거나 ALLOWLIST에 사유와 함께 등록하세요.")
                        }
                    }) { missing.shouldBeEmpty() }
                }
            }

            context("allowlist를 확인할 때") {
                it("더 이상 필요 없는 항목이 남아 있지 않아야 한다") {
                    val stale =
                        ALLOWLIST.flatMap { (moduleName, reasonByClassName) ->
                            val exempted =
                                modules
                                    .firstOrNull { it.name == moduleName }
                                    ?.writeServices
                                    ?.filter { !it.dispatches }
                                    ?.map { it.className }
                                    .orEmpty()
                            reasonByClassName.keys
                                .filterNot { it in exempted }
                                .map { "$moduleName > $it" }
                        }

                    withClue({
                        buildString {
                            appendLine("이벤트를 발행하거나 사라진 클래스가 allowlist에 남아 있습니다.")
                            stale.forEach { appendLine("  $it") }
                            append("모듈명 오타가 아닌지 확인하고 ALLOWLIST에서 제거하세요.")
                        }
                    }) { stale.shouldBeEmpty() }
                }
            }
        }
    }) {
    private data class WriteService(
        val className: String,
        val path: String,
        val dispatches: Boolean,
    )

    private data class ModuleScan(
        val name: String,
        val writeServices: List<WriteService>,
    )

    private companion object {
        const val EVENT_MARKER = "EventDispatchRequested"
        const val SETTINGS_FILE = "settings.gradle.kts"
        const val SOURCE_ROOT = "src/main/kotlin"

        val EVENT_DOMAINS = listOf("student", "club", "project")
        val TRANSACTIONAL_REGEX = Regex("""@Transactional[ \t]*(?:\(([^)]*)\))?""")
        val READ_ONLY_TRUE_REGEX = Regex("""readOnly\s*=\s*true""")
        val WRITE_SERVICE_PATH_REGEX =
            Regex("""/domain/(${EVENT_DOMAINS.joinToString("|")})/service/impl/[^/]+ServiceImpl\.kt$""")

        /**
         * 이벤트를 의도적으로 발행하지 않는 쓰기 서비스를 모듈명 → (클래스명 → 사유)로 등록한다.
         * 사유가 비어 있으면 면제되지 않으며, 더 이상 필요 없어진 항목은 테스트가 거부한다.
         */
        val ALLOWLIST: Map<String, Map<String, String>> =
            mapOf(
                // "datagsm-web" to
                //     mapOf("SomeServiceImpl" to "상위 서비스에서 일괄 발행하므로 중복 발행 방지"),
            )

        fun allowedClassNames(moduleName: String): Set<String> =
            ALLOWLIST[moduleName]
                .orEmpty()
                .filterValues { it.isNotBlank() }
                .keys

        fun findRepositoryRoot(): File =
            generateSequence(File("").absoluteFile) { it.parentFile }
                .firstOrNull { File(it, SETTINGS_FILE).isFile }
                ?: error("$SETTINGS_FILE 을 찾을 수 없습니다. 저장소 안에서 테스트를 실행해야 합니다.")

        fun scanModules(repositoryRoot: File): List<ModuleScan> =
            repositoryRoot
                .listFiles { file -> file.isDirectory && File(file, SOURCE_ROOT).isDirectory }
                .orEmpty()
                .sortedBy { it.name }
                .map { ModuleScan(it.name, scanWriteServices(it)) }
                .filter { it.writeServices.isNotEmpty() }

        fun scanWriteServices(moduleDirectory: File): List<WriteService> =
            File(moduleDirectory, SOURCE_ROOT)
                .walkTopDown()
                .filter { it.isFile && WRITE_SERVICE_PATH_REGEX.containsMatchIn(it.invariantSeparatorsPath) }
                .sortedBy { it.invariantSeparatorsPath }
                .mapNotNull { file ->
                    val source = file.readText()
                    if (!hasWriteTransaction(source)) {
                        null
                    } else {
                        WriteService(
                            className = file.nameWithoutExtension,
                            path = file.relativeTo(moduleDirectory).invariantSeparatorsPath,
                            dispatches = source.contains(EVENT_MARKER),
                        )
                    }
                }.toList()

        fun hasWriteTransaction(source: String): Boolean =
            TRANSACTIONAL_REGEX.findAll(source).any { match ->
                !READ_ONLY_TRUE_REGEX.containsMatchIn(match.groupValues[1])
            }
    }
}
