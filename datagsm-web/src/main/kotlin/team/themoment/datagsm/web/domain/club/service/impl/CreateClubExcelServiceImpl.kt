package team.themoment.datagsm.web.domain.club.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.dto.club.internal.ExcelRowDto
import team.themoment.datagsm.web.domain.club.service.CreateClubExcelService
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class CreateClubExcelServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : CreateClubExcelService {
    companion object {
        private const val MAJOR_CLUB_COL_IDX = 0
        private const val MAJOR_CLUB_LEADER_COL_IDX = 1
        private const val JOB_CLUB_COL_IDX = 2
        private const val JOB_CLUB_LEADER_COL_IDX = 3
        private const val AUTONOMOUS_CLUB_COL_IDX = 4
        private const val AUTONOMOUS_CLUB_LEADER_COL_IDX = 5

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    override fun execute(): ResponseEntity<ByteArray> {
        val data: List<ExcelRowDto> = getClubData()
        val workbook = XSSFWorkbook()
        try {
            val sheet = workbook.createSheet("동아리")
            val headerRow = sheet.createRow(0)

            headerRow.createCell(MAJOR_CLUB_COL_IDX).setCellValue(ClubType.MAJOR_CLUB.value)
            headerRow.createCell(MAJOR_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.MAJOR_CLUB.value} 부장")
            headerRow.createCell(JOB_CLUB_COL_IDX).setCellValue(ClubType.JOB_CLUB.value)
            headerRow.createCell(JOB_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.JOB_CLUB.value} 부장")
            headerRow.createCell(AUTONOMOUS_CLUB_COL_IDX).setCellValue(ClubType.AUTONOMOUS_CLUB.value)
            headerRow.createCell(AUTONOMOUS_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.AUTONOMOUS_CLUB.value} 부장")

            val maxRows = data.maxOf { it.clubName.size }
            val clubDataMap = data.associateBy { it.clubType }
            val clubTypeColumnMap =
                mapOf(
                    ClubType.MAJOR_CLUB to (MAJOR_CLUB_COL_IDX to MAJOR_CLUB_LEADER_COL_IDX),
                    ClubType.JOB_CLUB to (JOB_CLUB_COL_IDX to JOB_CLUB_LEADER_COL_IDX),
                    ClubType.AUTONOMOUS_CLUB to (AUTONOMOUS_CLUB_COL_IDX to AUTONOMOUS_CLUB_LEADER_COL_IDX),
                )

            for (rowIdx in 0 until maxRows) {
                val dataRow = sheet.createRow(rowIdx + 1)
                clubTypeColumnMap.forEach { (clubType, columnIndices) ->
                    val (clubNameColIdx, clubLeaderColIdx) = columnIndices
                    val clubData = clubDataMap[clubType]
                    clubData?.clubName?.getOrNull(rowIdx)?.let { clubName ->
                        dataRow.createCell(clubNameColIdx).setCellValue(clubName)
                    }
                    clubData?.clubLeader?.getOrNull(rowIdx)?.let { clubLeader ->
                        dataRow.createCell(clubLeaderColIdx).setCellValue(clubLeader)
                    }
                }
            }

            sheet.autoSizeColumn(MAJOR_CLUB_COL_IDX)
            sheet.autoSizeColumn(MAJOR_CLUB_LEADER_COL_IDX)
            sheet.autoSizeColumn(JOB_CLUB_COL_IDX)
            sheet.autoSizeColumn(JOB_CLUB_LEADER_COL_IDX)
            sheet.autoSizeColumn(AUTONOMOUS_CLUB_COL_IDX)
            sheet.autoSizeColumn(AUTONOMOUS_CLUB_LEADER_COL_IDX)

            val byteArrayFile =
                ByteArrayOutputStream().use { outputStream ->
                    workbook.write(outputStream)
                    outputStream.toByteArray()
                }

            val fileName = "동아리_현황_${LocalDate.now(ZoneId.of("Asia/Seoul"))
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
        } finally {
            workbook.close()
        }
    }

    private fun getClubData(): List<ExcelRowDto> =
        ClubType.entries.map { clubType ->
            val clubs = clubJpaRepository.findByType(clubType)
            val clubNames = clubs.map { it.name }
            val clubLeaders =
                clubs.map { club ->
                    "${club.leader.studentNumber.fullStudentNumber} ${club.leader.name}"
                }
            ExcelRowDto(clubName = clubNames, clubLeader = clubLeaders, clubType = clubType)
        }
}
