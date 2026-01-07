package team.themoment.datagsm.web.domain.student.service

import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.web.global.common.response.dto.response.CommonApiResponse

interface ModifyStudentExcelService {
    fun execute(file: MultipartFile): CommonApiResponse<Nothing>
}
