package team.themoment.datagsm.domain.club.service

import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

interface ModifyClubExcelService {
    fun execute(file: MultipartFile): CommonApiResponse<Nothing>
}
