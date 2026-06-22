package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonProperty
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

data class StudentEventSnapshot(
    @field:JsonProperty("index")
    val index: Int,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("email")
    val email: String,
    @field:JsonProperty("sex")
    val sex: String,
    @field:JsonProperty("grade")
    val grade: Int?,
    @field:JsonProperty("class_num")
    val classNum: Int?,
    @field:JsonProperty("number")
    val number: Int?,
    @field:JsonProperty("student_number")
    val studentNumber: Int?,
    @field:JsonProperty("major")
    val major: String?,
    @field:JsonProperty("specialty")
    val specialty: String?,
    @field:JsonProperty("role")
    val role: String,
    @field:JsonProperty("dormitory_floor")
    val dormitoryFloor: Int?,
    @field:JsonProperty("dormitory_room")
    val dormitoryRoom: Int?,
    @field:JsonProperty("major_club_name")
    val majorClubName: String?,
    @field:JsonProperty("autonomous_club_name")
    val autonomousClubName: String?,
    @field:JsonProperty("github_id")
    val githubId: String?,
) {
    companion object {
        fun from(
            index: Int,
            student: StudentJpaEntity,
        ): StudentEventSnapshot =
            StudentEventSnapshot(
                index = index,
                name = student.name,
                email = student.email,
                sex = student.sex.name,
                grade = student.studentNumber?.studentGrade,
                classNum = student.studentNumber?.studentClass,
                number = student.studentNumber?.studentNumber,
                studentNumber = student.studentNumber?.fullStudentNumber,
                major = student.major?.name,
                specialty = student.specialty,
                role = student.role.name,
                dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
                dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
                majorClubName = student.majorClub?.name,
                autonomousClubName = student.autonomousClub?.name,
                githubId = student.githubId,
            )
    }
}
