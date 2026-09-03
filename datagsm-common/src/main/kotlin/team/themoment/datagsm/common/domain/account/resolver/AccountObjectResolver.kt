package team.themoment.datagsm.common.domain.account.resolver

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.account.dto.internal.ResolvedAccountObject
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.project.dto.internal.ProjectSummaryDto
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.domain.teacher.dto.response.TeacherResDto
import team.themoment.datagsm.common.domain.teacher.repository.TeacherJpaRepository

/**
 * 계정의 다형 연결 대상(학생/선생님)을 objectType 기준으로 조회해 응답 DTO로 변환합니다.
 */
@Component
class AccountObjectResolver(
    private val studentJpaRepository: StudentJpaRepository,
    private val teacherJpaRepository: TeacherJpaRepository,
    private val projectJpaRepository: ProjectJpaRepository,
) {
    fun resolve(
        account: AccountJpaEntity,
        includeClubs: Boolean = true,
        includeProjects: Boolean = true,
    ): ResolvedAccountObject {
        val objectId = account.objectId ?: return ResolvedAccountObject(null, null)
        return when (account.objectType) {
            AccountObjectType.STUDENT -> {
                val student = studentJpaRepository.findById(objectId).orElse(null)
                ResolvedAccountObject(
                    student = student?.let { StudentResDto.from(it) },
                    teacher = null,
                    clubs = if (includeClubs) student?.let { resolveClubs(it) } ?: emptyList() else emptyList(),
                    projects = if (includeProjects) student?.let { resolveProjects(it) } ?: emptyList() else emptyList(),
                )
            }
            AccountObjectType.TEACHER ->
                ResolvedAccountObject(
                    student = null,
                    teacher = teacherJpaRepository.findById(objectId).map { TeacherResDto.from(it) }.orElse(null),
                )
            null -> ResolvedAccountObject(null, null)
        }
    }

    private fun resolveClubs(student: StudentJpaEntity): List<ClubSummaryDto> =
        listOfNotNull(student.majorClub, student.autonomousClub).map { it.toSummaryDto() }

    private fun resolveProjects(student: StudentJpaEntity): List<ProjectSummaryDto> =
        projectJpaRepository.findAllByParticipantId(student.id!!).map { it.toSummaryDto() }

    private fun ClubJpaEntity.toSummaryDto() = ClubSummaryDto(id = id!!, name = name, type = type)

    private fun ProjectJpaEntity.toSummaryDto() = ProjectSummaryDto(id = id!!, name = name, status = status, club = club?.toSummaryDto())

    fun resolveAll(accounts: List<AccountJpaEntity>): Map<Long, ResolvedAccountObject> {
        val studentIds =
            accounts
                .filter { it.objectType == AccountObjectType.STUDENT && it.objectId != null }
                .map { it.objectId!! }
        val teacherIds =
            accounts
                .filter { it.objectType == AccountObjectType.TEACHER && it.objectId != null }
                .map { it.objectId!! }

        val studentDtos =
            studentJpaRepository
                .findAllByIdInWithClubs(studentIds)
                .associate { it.id!! to StudentResDto.from(it) }
        val teacherDtos =
            teacherJpaRepository
                .findAllById(teacherIds)
                .associate { it.id!! to TeacherResDto.from(it) }

        return accounts.associate { account ->
            val objectId = account.objectId
            val resolved =
                when (account.objectType) {
                    AccountObjectType.STUDENT -> ResolvedAccountObject(studentDtos[objectId], null)
                    AccountObjectType.TEACHER -> ResolvedAccountObject(null, teacherDtos[objectId])
                    null -> ResolvedAccountObject(null, null)
                }
            account.id!! to resolved
        }
    }
}
