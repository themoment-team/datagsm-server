package team.themoment.datagsm.web.domain.project.mapper

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.project.dto.request.ApplyProjectReqDto
import team.themoment.datagsm.common.domain.project.entity.ProjectEditRequestJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.sdk.exception.ExpectedException

@Component
class ProjectApplicationAssembler(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
    private val projectIconStorage: ProjectIconStorage,
) {
    fun applyTo(
        request: ProjectEditRequestJpaEntity,
        reqDto: ApplyProjectReqDto,
    ) {
        request.name = reqDto.name
        request.description = reqDto.description
        request.startYear = reqDto.startYear
        request.club = resolveClub(reqDto.clubId)
        request.participants = resolveParticipants(reqDto.participantIds)
        request.repositories = reqDto.repositories.toMutableSet()
        request.techStacks = reqDto.techStacks.toMutableSet()
        // 값이 없으면 기존 아이콘과 배포 URL을 유지한다. 매번 다시 올리지 않아도 되도록 하기 위함이다
        reqDto.iconKey?.let { request.iconKey = projectIconStorage.validateIconKey(it) }
        reqDto.deploymentUrl?.let { request.deploymentUrl = it }
    }

    /** 클라이언트는 무소속을 0으로 보내므로 null과 동일하게 취급한다 */
    private fun resolveClub(clubId: Long?): ClubJpaEntity? {
        if (clubId == null || clubId == 0L) return null
        return clubJpaRepository
            .findById(clubId)
            .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND) }
    }

    private fun resolveParticipants(participantIds: List<Long>): MutableSet<StudentJpaEntity> {
        if (participantIds.isEmpty()) return mutableSetOf()

        val foundStudents = studentJpaRepository.findAllById(participantIds).toMutableSet()
        if (foundStudents.size != participantIds.toSet().size) {
            throw ExpectedException("해당 학생 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        return foundStudents
    }
}
