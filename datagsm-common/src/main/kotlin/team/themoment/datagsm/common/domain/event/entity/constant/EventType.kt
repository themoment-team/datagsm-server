package team.themoment.datagsm.common.domain.event.entity.constant

enum class EventType(
    val eventName: String,
    val description: String,
) {
    STUDENT_UPDATED("student.updated", "학생 정보 업데이트"),
    CLUB_UPDATED("club.updated", "동아리 정보 업데이트"),
    PROJECT_UPDATED("project.updated", "프로젝트 정보 업데이트"),
}
