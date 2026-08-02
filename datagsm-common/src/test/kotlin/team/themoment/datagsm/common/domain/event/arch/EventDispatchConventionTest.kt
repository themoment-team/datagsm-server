package team.themoment.datagsm.common.domain.event.arch

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.io.File

private const val EVENT_MARKER = "EventDispatchRequested"
private const val ALLOWLIST_NAME = "event-dispatch-allowlist.txt"
private const val COMMENT_PREFIX = "#"
private const val SETTINGS_FILE = "settings.gradle.kts"
private const val SOURCE_ROOT = "src/main/kotlin"
private val EVENT_DOMAINS = listOf("student", "club", "project")
private val TRANSACTIONAL_REGEX = Regex("""@Transactional[ \t]*(?:\(([^)]*)\))?""")
private val READ_ONLY_TRUE_REGEX = Regex("""readOnly\s*=\s*true""")
private val WRITE_SERVICE_PATH_REGEX =
    Regex("""/domain/(${EVENT_DOMAINS.joinToString("|")})/service/impl/[^/]+ServiceImpl\.kt$""")

private data class WriteService(
    val className: String,
    val path: String,
    val dispatches: Boolean,
)

private data class ModuleScan(
    val name: String,
    val writeServices: List<WriteService>,
    val reasonByClassName: Map<String, String>,
    val entriesWithoutReason: List<String>,
)

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
                            module.writeServices
                                .filter { !it.dispatches && it.className !in module.reasonByClassName }
                                .map { "${module.name}/${it.path}" }
                        }

                    withClue({
                        buildString {
                            appendLine("이벤트 발행이 누락되었습니다.")
                            missing.forEach { appendLine("  $it") }
                            append("발행을 추가하거나 해당 모듈의 $ALLOWLIST_NAME 에 ")
                            append("'<ClassName> $COMMENT_PREFIX <사유>' 형식으로 등록하세요.")
                        }
                    }) { missing.shouldBeEmpty() }
                }
            }

            context("allowlist를 확인할 때") {
                it("모든 항목이 사유를 가지고 있어야 한다") {
                    val withoutReason =
                        modules.flatMap { module ->
                            module.entriesWithoutReason.map { "${module.name}/$ALLOWLIST_NAME $it" }
                        }

                    withClue({
                        buildString {
                            appendLine("사유가 없는 allowlist 항목이 있습니다.")
                            withoutReason.forEach { appendLine("  $it") }
                            append("'<ClassName> $COMMENT_PREFIX <사유>' 형식으로 사유를 남겨야 합니다.")
                        }
                    }) { withoutReason.shouldBeEmpty() }
                }

                it("더 이상 필요 없는 항목이 남아 있지 않아야 한다") {
                    val stale =
                        modules.flatMap { module ->
                            module.reasonByClassName.keys
                                .filter { className ->
                                    module.writeServices.none { it.className == className && !it.dispatches }
                                }.map { "${module.name}/$ALLOWLIST_NAME > $it" }
                        }

                    withClue({
                        buildString {
                            appendLine("이벤트를 발행하거나 사라진 클래스가 allowlist에 남아 있습니다.")
                            stale.forEach { appendLine("  $it") }
                            append("$ALLOWLIST_NAME 에서 제거하세요.")
                        }
                    }) { stale.shouldBeEmpty() }
                }
            }
        }
    })

private fun findRepositoryRoot(): File =
    generateSequence(File("").absoluteFile) { it.parentFile }
        .firstOrNull { File(it, SETTINGS_FILE).isFile }
        ?: error("$SETTINGS_FILE 을 찾을 수 없습니다. 저장소 안에서 테스트를 실행해야 합니다.")

private fun scanModules(repositoryRoot: File): List<ModuleScan> =
    repositoryRoot
        .listFiles { file -> file.isDirectory && File(file, SOURCE_ROOT).isDirectory }
        .orEmpty()
        .sortedBy { it.name }
        .map { moduleDirectory ->
            val allowlist = readAllowlist(File(moduleDirectory, ALLOWLIST_NAME))
            ModuleScan(
                name = moduleDirectory.name,
                writeServices = scanWriteServices(moduleDirectory),
                reasonByClassName = allowlist.first,
                entriesWithoutReason = allowlist.second,
            )
        }.filter { it.writeServices.isNotEmpty() || it.reasonByClassName.isNotEmpty() }

private fun scanWriteServices(moduleDirectory: File): List<WriteService> =
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

private fun hasWriteTransaction(source: String): Boolean =
    TRANSACTIONAL_REGEX.findAll(source).any { match ->
        !READ_ONLY_TRUE_REGEX.containsMatchIn(match.groupValues[1])
    }

private fun readAllowlist(file: File): Pair<Map<String, String>, List<String>> {
    if (!file.isFile) return emptyMap<String, String>() to emptyList()

    val reasonByClassName = LinkedHashMap<String, String>()
    val entriesWithoutReason = mutableListOf<String>()

    file.readLines().forEachIndexed { index, rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) return@forEachIndexed

        val className = line.substringBefore(COMMENT_PREFIX).trim()
        val reason = line.substringAfter(COMMENT_PREFIX, "").trim()
        if (reason.isEmpty()) {
            entriesWithoutReason += "line ${index + 1} > $line"
        } else {
            reasonByClassName[className] = reason
        }
    }

    return reasonByClassName to entriesWithoutReason
}
