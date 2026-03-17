package team.themoment.datagsm.web.domain.club.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
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
        private const val CLUB_NAME_COL_IDX = 0
        private const val CLUB_TYPE_COL_IDX = 1
        private const val LEADER_COL_IDX = 2
        private const val FOUNDED_YEAR_COL_IDX = 3
        private const val STATUS_COL_IDX = 4
        private const val ABOLISHED_YEAR_COL_IDX = 5

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    @Transactional(readOnly = true)
    override fun execute(): ResponseEntity<ByteArray> {
        val data: List<ClubInfoDto> = getClubData()
        val workbook = XSSFWorkbook()
        workbook.use { workbook ->
            val sheet = workbook.createSheet("동아리")
            val headerRow = sheet.createRow(0)

            headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
            headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
            headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
            headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
            headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
            headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

            data.forEachIndexed { index, dto ->
                val dataRow = sheet.createRow(index + 1)
                dataRow.createCell(CLUB_NAME_COL_IDX).setCellValue(dto.clubName)
                dataRow.createCell(CLUB_TYPE_COL_IDX).setCellValue(dto.clubType.name)
                dataRow.createCell(LEADER_COL_IDX).setCellValue(dto.leaderInfo ?: "")
                dataRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(dto.foundedYear.toDouble())
                dataRow.createCell(STATUS_COL_IDX).setCellValue(dto.status.name)
                val abolishedYear = dto.abolishedYear
                if (abolishedYear != null) {
                    dataRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue(abolishedYear.toDouble())
                } else {
                    dataRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")
                }
            }

            for (i in 0..ABOLISHED_YEAR_COL_IDX) {
                sheet.autoSizeColumn(i)
            }

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
        clubJpaRepository.findAll().map { club ->
            val leaderStr =
                club.leader
                    ?.let { leader ->
                        leader.studentNumber
                            ?.fullStudentNumber
                            ?.let { "$it " }
                            .orEmpty() + leader.name
                    }
            ClubInfoDto(
                clubName = club.name,
                clubType = club.type,
                leaderInfo = if (club.status == ClubStatus.ABOLISHED) null else leaderStr,
                foundedYear = club.foundedYear,
                status = club.status,
                abolishedYear = club.abolishedYear,
            )
        }
}
