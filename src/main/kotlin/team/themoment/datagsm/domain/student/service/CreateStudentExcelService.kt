package team.themoment.datagsm.domain.student.service

import org.springframework.http.ResponseEntity

interface CreateStudentExcelService {
    fun execute(): ResponseEntity<ByteArray>
}
