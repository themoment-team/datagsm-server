package team.themoment.datagsm.domain.club.service

import team.themoment.datagsm.domain.club.dto.internal.ExcelRowDto

interface CreateClubExcelService {
    fun execute(): ByteArray

    fun getClubData(): List<ExcelRowDto>
}
