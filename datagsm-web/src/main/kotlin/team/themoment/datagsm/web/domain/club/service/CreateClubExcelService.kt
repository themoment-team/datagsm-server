package team.themoment.datagsm.web.domain.club.service

import org.springframework.http.ResponseEntity

interface CreateClubExcelService {
    fun execute(): ResponseEntity<ByteArray>
}
