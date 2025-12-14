package team.themoment.datagsm.domain.student.service

import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

interface ModifyStudentExcelService {
    fun execute(file: MultipartFile): CommonApiResponse<Nothing>

    fun queryExcelData(file: MultipartFile): List<ExcelRowDto>
}
