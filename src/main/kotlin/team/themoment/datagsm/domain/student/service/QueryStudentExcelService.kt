package team.themoment.datagsm.domain.student.service

import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto

interface QueryStudentExcelService {
    fun queryStudentData(file: MultipartFile)
    fun queryExcelData(file: MultipartFile): List<ExcelRowDto>
}
