package team.themoment.datagsm.domain.club.service

import org.springframework.http.ResponseEntity
import team.themoment.datagsm.domain.club.dto.internal.ExcelRowDto

interface CreateClubExcelService {
    fun execute(): ResponseEntity<ByteArray>

    fun getClubData(): List<ExcelRowDto>
}
