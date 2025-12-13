package team.themoment.datagsm.domain.club.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.CreateClubExcelService
import java.io.ByteArrayOutputStream

@Service
@Transactional(readOnly = true)
class CreateClubExcelServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : CreateClubExcelService {
    companion object {
        private const val MAJOR_CLUB_COL_IDX = 0
        private const val JOB_CLUB_COL_IDX = 1
        private const val AUTONOMOUS_CLUB_COL_IDX = 2
    }

    override fun createExcel(): ByteArray {
        val data: List<ExcelRowDto> = getClubData()
        val workbook = XSSFWorkbook()

        val sheet = workbook.createSheet("동아리")
        val headerRow = sheet.createRow(0)

        headerRow.createCell(MAJOR_CLUB_COL_IDX).setCellValue("전공동아리")
        headerRow.createCell(JOB_CLUB_COL_IDX).setCellValue("취업동아리")
        headerRow.createCell(AUTONOMOUS_CLUB_COL_IDX).setCellValue("창체동아리")

        val maxRows = data.maxOf { it.clubName.size }

        for(rowIdx in 0 until maxRows) {
            val dataRow = sheet.createRow(rowIdx + 1)

            if(rowIdx < data[0].clubName.size) {
                dataRow.createCell(MAJOR_CLUB_COL_IDX)
                    .setCellValue(data[0].clubName[rowIdx])
            }

            if(rowIdx < data[1].clubName.size) {
                dataRow.createCell(JOB_CLUB_COL_IDX)
                    .setCellValue(data[1].clubName[rowIdx])
            }

            if(rowIdx < data[2].clubName.size) {
                dataRow.createCell(AUTONOMOUS_CLUB_COL_IDX)
                    .setCellValue(data[2].clubName[rowIdx])
            }
        }

        sheet.autoSizeColumn(MAJOR_CLUB_COL_IDX)
        sheet.autoSizeColumn(JOB_CLUB_COL_IDX)
        sheet.autoSizeColumn(AUTONOMOUS_CLUB_COL_IDX)

        return ByteArrayOutputStream().use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
            outputStream.toByteArray()
        }
    }

    override fun getClubData(): List<ExcelRowDto> =
        ClubType.entries.map { clubType ->
            val clubNames = clubJpaRepository.findByType(clubType).map { it.name }
            ExcelRowDto(clubName = clubNames, clubType = clubType)
        }
}

