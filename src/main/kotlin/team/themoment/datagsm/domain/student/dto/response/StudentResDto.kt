package team.themoment.datagsm.domain.student.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex

data class StudentResDto(
    @param:Schema(description = "학생 ID", example = "1")
    val studentId: Long,
    @param:Schema(description = "이름", example = "홍길동")
    val name: String,
    @param:Schema(description = "성별", example = "MALE")
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
    @param:Schema(description = "전공", example = "SW")
    val major: Major,
    @param:Schema(description = "역할", example = "GENERAL_STUDENT")
    val role: Role,
    @param:Schema(description = "기숙사 층", example = "3")
    val dormitoryFloor: Int,
    @param:Schema(description = "기숙사 호실", example = "301")
    val dormitoryRoom: Int,
    @param:Schema(description = "자퇴 여부", example = "false")
    val isLeaveSchool: Boolean,
)
