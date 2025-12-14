package team.themoment.datagsm.domain.student.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.internal.ExcelColumnDto
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.CreateStudentExcelService
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class CreateStudentExcelServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : CreateStudentExcelService {
    companion object {
        private const val NAME_COL_IDX = 0
        private const val STUDENT_NUMBER_COL_IDX = 1
        private const val EMAIL_COL_IDX = 2
        private const val MAJOR_COL_IDX = 3
        private const val MAJOR_CLUB_COL_IDX = 4
        private const val JOB_CLUB_COL_IDX = 5
        private const val AUTONOMOUS_COL_IDX = 6
        private const val DOROMITORY_ROOM_NUMBER_COL_IDX = 7
        private const val STUDENT_ROLE_COL_IDX = 8
        private const val IS_SCHOOL_LEAVE_COL_IDX = 9
        private const val SEX_COL_IDX = 10

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override fun execute(): ResponseEntity<ByteArray> {
        val data: List<ExcelRowDto> = getStudentData()
        val workbook = XSSFWorkbook()

        data.forEachIndexed { idx, excelRowDto ->
            val sheet = workbook.createSheet("${idx + 1}학년")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(NAME_COL_IDX).setCellValue("학생명")
            headerRow.createCell(STUDENT_NUMBER_COL_IDX).setCellValue("학번")
            headerRow.createCell(EMAIL_COL_IDX).setCellValue("이메일")
            headerRow.createCell(MAJOR_COL_IDX).setCellValue("학과")
            headerRow.createCell(MAJOR_CLUB_COL_IDX).setCellValue("전공동아리")
            headerRow.createCell(JOB_CLUB_COL_IDX).setCellValue("취업동아리")
            headerRow.createCell(AUTONOMOUS_COL_IDX).setCellValue("창체동아리")
            headerRow.createCell(DOROMITORY_ROOM_NUMBER_COL_IDX).setCellValue("호실")
            headerRow.createCell(STUDENT_ROLE_COL_IDX).setCellValue("소속")
            headerRow.createCell(IS_SCHOOL_LEAVE_COL_IDX).setCellValue("자퇴 여부")
            headerRow.createCell(SEX_COL_IDX).setCellValue("성별")

            excelRowDto.excelRows.forEachIndexed { rowIndex, columnDto ->
                val row = sheet.createRow(rowIndex + 1)

                row.createCell(NAME_COL_IDX).setCellValue(columnDto.name)
                row.createCell(STUDENT_NUMBER_COL_IDX).setCellValue(columnDto.number.toString())
                row.createCell(EMAIL_COL_IDX).setCellValue(columnDto.email)
                row.createCell(MAJOR_COL_IDX).setCellValue(columnDto.major.value)
                row.createCell(MAJOR_CLUB_COL_IDX).setCellValue(columnDto.majorClub ?: "")
                row.createCell(JOB_CLUB_COL_IDX).setCellValue(columnDto.jobClub ?: "")
                row.createCell(AUTONOMOUS_COL_IDX).setCellValue(columnDto.autonomousClub ?: "")
                row.createCell(DOROMITORY_ROOM_NUMBER_COL_IDX)
                    .setCellValue(columnDto.dormitoryRoomNumber?.toString() ?: "")
                row.createCell(STUDENT_ROLE_COL_IDX).setCellValue(columnDto.role.value)
                row.createCell(IS_SCHOOL_LEAVE_COL_IDX).setCellValue(if (columnDto.isLeaveSchool) "O" else "X")
                row.createCell(SEX_COL_IDX).setCellValue(columnDto.sex.value)
            }
        }

        val byteArrayFile = ByteArrayOutputStream().use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
            outputStream.toByteArray()
        }

        val fileName = "학생_현황_${LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DATE_FORMATTER)}.xlsx"

        val headers =
            HttpHeaders().apply {
                contentType =
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                contentDisposition =
                    ContentDisposition
                        .builder("attachment")
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
            }

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(byteArrayFile)
    }

    private fun getStudentData(): List<ExcelRowDto> {
        val data = mutableListOf<ExcelRowDto>()
        for (i: Int in 1..3) {
            val list = studentJpaRepository.findStudentsByGrade(i)
            val excelRowDto =
                ExcelRowDto(
                    excelRows =
                        list.map { student ->
                            ExcelColumnDto(
                                name = student.name,
                                number = student.studentNumber.fullStudentNumber,
                                email = student.email,
                                major = student.major,
                                majorClub = student.majorClub?.name,
                                jobClub = student.jobClub?.name,
                                autonomousClub = student.autonomousClub?.name,
                                dormitoryRoomNumber = student.dormitoryRoomNumber?.dormitoryRoomNumber,
                                role = student.role,
                                isLeaveSchool = student.isLeaveSchool,
                                sex = student.sex,
                            )
                        },
                )
            data.add(excelRowDto)
        }
        return data.toList()
    }
}
