package team.themoment.datagsm.domain.student.service

import org.springframework.http.ResponseEntity
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto

interface CreateStudentExcelService {
    fun execute(): ResponseEntity<ByteArray>

    fun getStudentData(): List<ExcelRowDto>
}
