package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.ksp.annotation.SdkExport

/** 프로젝트 참여자 선택용 학생 정보 — 동명이인 구분에 필요한 정보만 노출한다 */
@SdkExport
data class ParticipantCandidateResDto(
    @field:Schema(description = "학생 ID", example = "1")
    val id: Long,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "학번", example = "2105")
    val studentNumber: Int?,
    @field:Schema(description = "학과", example = "SW_DEVELOPMENT")
    val major: Major?,
) {
    companion object {
        fun from(student: StudentJpaEntity): ParticipantCandidateResDto =
            ParticipantCandidateResDto(
                id = student.id!!,
                name = student.name,
                studentNumber = student.studentNumber?.fullStudentNumber,
                major = student.major,
            )
    }
}
