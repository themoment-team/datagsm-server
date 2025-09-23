package team.themoment.datagsm.domain.student.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.response.StudentReqDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.service.QueryStudentService

@RestController
@RequestMapping("/v1/students")
class StudentController(
    private val queryStudentService: QueryStudentService,
) {
    @GetMapping
    fun getStudentInfo(
        @RequestParam(required = false) studentId: Long?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNum: Int?,
        @RequestParam(required = false) number: Int?,
        @RequestParam(required = false) sex: Sex?,
        @RequestParam(required = false) role: Role?,
        @RequestParam(required = false) dormitoryRoom: Int?,
        @RequestParam(required = false, defaultValue = "false") isLeaveSchool: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "300") size: Int,
    ): StudentReqDto =
        queryStudentService.execute(
            studentId,
            name,
            email,
            grade,
            classNum,
            number,
            sex,
            role,
            dormitoryRoom,
            isLeaveSchool,
            page,
            size,
        )
}
