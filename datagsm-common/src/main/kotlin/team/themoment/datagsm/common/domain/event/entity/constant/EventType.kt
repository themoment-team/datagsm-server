package team.themoment.datagsm.common.domain.event.entity.constant

enum class EventType(
    val eventName: String,
    val description: String,
) {
    STUDENT_CHANGED("student.changed", "학생 정보 변경"),
    CLUB_CREATED("club.created", "동아리 생성"),
    CLUB_UPDATED("club.updated", "동아리 정보 수정"),
    CLUB_DELETED("club.deleted", "동아리 삭제"),
    PROJECT_CREATED("project.created", "프로젝트 생성"),
    PROJECT_UPDATED("project.updated", "프로젝트 수정"),
    PROJECT_DELETED("project.deleted", "프로젝트 삭제"),
}
