package team.themoment.datagsm.common.domain.event.entity.constant

enum class EventType(
    val eventName: String,
    val description: String,
) {
    STUDENT_CHANGED("student.changed", "학생 정보 변경"),
    CLUB_CHANGED("club.changed", "동아리 정보 변경"),
    PROJECT_CHANGED("project.changed", "프로젝트 정보 변경"),
}
