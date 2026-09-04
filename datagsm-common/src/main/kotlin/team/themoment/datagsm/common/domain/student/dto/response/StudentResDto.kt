package team.themoment.datagsm.common.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.club.dto.internal.ClubSummaryDto
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class StudentResDto(
    @field:Schema(description = "학생 ID", example = "1")
    val id: Long,
    @field:Schema(description = "이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "성별", example = "WOMAN")
    val sex: Sex,
    @field:Schema(description = "이메일", example = "student@gsm.hs.kr")
    val email: String,
    @field:Schema(description = "학년", example = "1")
    val grade: Int?,
    @field:Schema(description = "반", example = "1")
    val classNum: Int?,
    @field:Schema(description = "번호", example = "1")
    val number: Int?,
    @field:Schema(description = "학번", example = "1101")
    val studentNumber: Int?,
    @field:Schema(description = "학과", example = "SW_DEVELOPMENT", allowableValues = ["SW_DEVELOPMENT", "SMART_IOT", "AI"])
    val major: Major?,
    @field:Schema(description = "전공", example = "백엔드")
    val specialty: String?,
    @field:Schema(description = "역할", example = "GENERAL_STUDENT")
    val role: StudentRole,
    @field:Schema(description = "기숙사 층", example = "3")
    val dormitoryFloor: Int?,
    @field:Schema(description = "기숙사 호실", example = "301")
    val dormitoryRoom: Int?,
    @field:Schema(description = "전공 동아리")
    val majorClub: ClubSummaryDto?,
    @field:Schema(description = "자율 동아리")
    val autonomousClub: ClubSummaryDto?,
    @field:Schema(description = "GitHub 아이디", example = "torvalds")
    val githubId: String?,
    @field:Schema(description = "GitHub 프로필 URL", example = "https://github.com/torvalds")
    val githubUrl: String?,
) {
    companion object {
        fun from(student: StudentJpaEntity): StudentResDto =
            StudentResDto(
                id = student.id!!,
                name = student.name,
                sex = student.sex,
                email = student.email,
                grade = student.studentNumber?.studentGrade,
                classNum = student.studentNumber?.studentClass,
                number = student.studentNumber?.studentNumber,
                studentNumber = student.studentNumber?.fullStudentNumber,
                major = student.major,
                specialty = student.specialty,
                role = student.role,
                dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
                dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
                majorClub = student.majorClub?.let { ClubSummaryDto.from(it) },
                autonomousClub = student.autonomousClub?.let { ClubSummaryDto.from(it) },
                githubId = student.githubId,
                githubUrl = student.githubId?.let { "https://github.com/$it" },
            )
    }
}
