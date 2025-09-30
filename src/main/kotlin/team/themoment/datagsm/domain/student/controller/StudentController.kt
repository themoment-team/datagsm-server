package team.themoment.datagsm.domain.student.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.dto.request.StudentCreateReqDto
import team.themoment.datagsm.domain.student.dto.request.StudentUpdateReqDto
import team.themoment.datagsm.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.service.CreateStudentService
import team.themoment.datagsm.domain.student.service.ModifyStudentService
import team.themoment.datagsm.domain.student.service.QueryStudentService

@RestController
@RequestMapping("/v1/students")
class StudentController(
    private final val queryStudentService: QueryStudentService,
    private final val createStudentService: CreateStudentService,
    private final val modifyStudentService: ModifyStudentService,
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
    ): StudentResDto =
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

    @PostMapping
    fun createStudent(
        @RequestBody @Valid reqDto: StudentCreateReqDto,
    ): StudentResDto = createStudentService.createStudent(reqDto)

    @PatchMapping("/{studentId}")
    fun updateStudent(
        @PathVariable studentId: Long,
        @RequestBody @Valid reqDto: StudentUpdateReqDto,
    ): StudentResDto = modifyStudentService.execute(studentId, reqDto)
}
