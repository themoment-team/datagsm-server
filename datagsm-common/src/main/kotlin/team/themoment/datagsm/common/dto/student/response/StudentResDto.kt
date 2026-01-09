package team.themoment.datagsm.common.dto.student.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.dto.club.internal.ClubSummaryDto

data class StudentResDto(
    @param:Schema(description = "학생 ID", example = "1")
    val id: Long,
    @param:Schema(description = "이름", example = "홍길동")
    val name: String,
    @param:Schema(description = "성별", example = "WOMAN")
    val sex: Sex,
    @param:Schema(description = "이메일", example = "student@gsm.hs.kr")
    val email: String,
    @param:Schema(description = "학년", example = "1")
    val grade: Int,
    @param:Schema(description = "반", example = "1")
    val classNum: Int,
    @param:Schema(description = "번호", example = "1")
    val number: Int,
    @param:Schema(description = "학번", example = "1101")
    val studentNumber: Int,
    @param:Schema(description = "전공", example = "SW_DEVELOPMENT", allowableValues = ["SW_DEVELOPMENT", "SMART_IOT", "AI"])
    val major: Major,
    @param:Schema(description = "역할", example = "GENERAL_STUDENT")
    val role: StudentRole,
    @param:Schema(description = "기숙사 층", example = "3")
    val dormitoryFloor: Int?,
    @param:Schema(description = "기숙사 호실", example = "301")
    val dormitoryRoom: Int?,
    @param:Schema(description = "자퇴 여부", example = "false")
    val isLeaveSchool: Boolean,
    @param:Schema(description = "전공 동아리")
    val majorClub: ClubSummaryDto?,
    @param:Schema(description = "취업 동아리")
    val jobClub: ClubSummaryDto?,
    @param:Schema(description = "자율 동아리")
    val autonomousClub: ClubSummaryDto?,
)
