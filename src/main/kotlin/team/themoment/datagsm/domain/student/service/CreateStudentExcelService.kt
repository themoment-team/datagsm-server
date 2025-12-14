package team.themoment.datagsm.domain.student.service

import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto

interface CreateStudentExcelService {
    fun execute(): ByteArray

    fun getStudentData(): List<ExcelRowDto>
}
