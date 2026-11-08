package team.themoment.datagsm.web.domain.student.service

interface RevokeStudentLoginService {
    fun revokeAsync(studentIds: List<Long>)
}
