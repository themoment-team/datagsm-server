package team.themoment.datagsm.web.domain.club.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.CreateClubExcelService
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
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

    @Transactional(readOnly = true)
    override fun execute(): ResponseEntity<ByteArray> {
        val data: List<ClubInfoDto> = getClubData()
        val workbook = XSSFWorkbook()
        workbook.use { workbook ->
            val sheet = workbook.createSheet("동아리")
            val headerRow = sheet.createRow(0)

            headerRow.createCell(MAJOR_CLUB_COL_IDX).setCellValue(ClubType.MAJOR_CLUB.value)
            headerRow.createCell(MAJOR_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.MAJOR_CLUB.value} 부장")
            headerRow.createCell(JOB_CLUB_COL_IDX).setCellValue(ClubType.JOB_CLUB.value)
            headerRow.createCell(JOB_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.JOB_CLUB.value} 부장")
            headerRow.createCell(AUTONOMOUS_CLUB_COL_IDX).setCellValue(ClubType.AUTONOMOUS_CLUB.value)
            headerRow.createCell(AUTONOMOUS_CLUB_LEADER_COL_IDX).setCellValue("${ClubType.AUTONOMOUS_CLUB.value} 부장")

            val dataByType = data.groupBy { it.clubType }
            val maxRows = ClubType.entries.maxOf { dataByType[it]?.size ?: 0 }
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
                    dataByType[clubType]?.getOrNull(rowIdx)?.let { dto ->
                        dataRow.createCell(clubNameColIdx).setCellValue(dto.clubName)
                        dataRow.createCell(clubLeaderColIdx).setCellValue(dto.leaderInfo ?: "")
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
        }
    }

    private fun getClubData(): List<ClubInfoDto> =
        ClubType.entries.flatMap { clubType ->
            clubJpaRepository.findByType(clubType).map { club ->
                val leaderStr =
                    club.leader
                        ?.let { leader ->
                            leader.studentNumber
                                ?.fullStudentNumber
                                ?.let { "$it " }
                                .orEmpty() + leader.name
                        }
                ClubInfoDto(clubName = club.name, clubType = clubType, leaderInfo = leaderStr)
            }
        }
}
