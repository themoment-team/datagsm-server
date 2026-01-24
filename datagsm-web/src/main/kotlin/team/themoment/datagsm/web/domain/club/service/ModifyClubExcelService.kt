package team.themoment.datagsm.web.domain.club.service

import org.springframework.web.multipart.MultipartFile
import team.themoment.sdk.response.CommonApiResponse

interface ModifyClubExcelService {
    fun execute(file: MultipartFile): CommonApiResponse<Nothing>
}
