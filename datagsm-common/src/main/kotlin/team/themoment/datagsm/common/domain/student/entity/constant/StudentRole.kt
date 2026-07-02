package team.themoment.datagsm.common.domain.student.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class StudentRole(
    val value: String,
) {
    STUDENT_COUNCIL("학생회"),
    DORMITORY_MANAGER("기숙사자치위원회"),
    GENERAL_STUDENT("일반학생"),
    GRADUATE("졸업생"),
    WITHDRAWN("자퇴생"),
    ;

    companion object {
        fun fromRole(role: String): StudentRole? = entries.find { it.value == role }
    }
}
