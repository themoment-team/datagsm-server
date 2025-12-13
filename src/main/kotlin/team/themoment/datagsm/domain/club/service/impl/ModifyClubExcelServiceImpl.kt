package team.themoment.datagsm.domain.club.service.impl

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.service.ModifyClubExcelService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class ModifyClubExcelServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
) : ModifyClubExcelService {
    companion object {
        private const val MAJOR_CLUB_COL_IDX = 0
        private const val JOB_CLUB_COL_IDX = 1
        private const val AUTONOMOUS_CLUB_COL_IDX = 2
    }

    override fun modifyClubData(file: MultipartFile): CommonApiResponse<Nothing> {
        val excelData: List<ExcelRowDto> = queryExcelData(file)
        val clubNames = excelData.flatMap { row ->
            row.clubName.map { name ->
                ClubInfoDto(
                    clubName = name,
                    clubType = row.clubType
                )
            }
        }
        val existingClubs =
            clubJpaRepository.findAllByNameIn(clubNames.map { it.clubName })
                .associateBy { it.name }

        val clubsToSave =
            clubNames.map { dto ->
                (existingClubs[dto.clubName] ?: ClubJpaEntity()).also { club ->
                    club.name = dto.clubName
                    club.type = dto.clubType
                }
            }
        clubJpaRepository.saveAll(clubsToSave)
        return CommonApiResponse.success("엑셀 업로드 성공")
    }

    override fun queryExcelData(file: MultipartFile): List<ExcelRowDto> {
        val workbook =
            file.inputStream.use { inputStream ->
                when (file.originalFilename?.substringAfterLast(".")) {
                    "xlsx" -> XSSFWorkbook(inputStream)
                    "xls" -> HSSFWorkbook(inputStream)
                    else -> throw IllegalArgumentException("지원하지 않는 파일 형식입니다.")
                }
            }

        val sheet = workbook.getSheetAt(0)
        val headerRow = sheet?.getRow(0)
        val headerColumns = listOf(
            headerRow?.getCell(MAJOR_CLUB_COL_IDX)?.stringCellValue ?: "",
            headerRow?.getCell(JOB_CLUB_COL_IDX)?.stringCellValue ?: "",
            headerRow?.getCell(AUTONOMOUS_CLUB_COL_IDX)?.stringCellValue ?: "",
        )
        val expectedHeaders = listOf("전공동아리", "취업동아리", "창체동아리")
        try {
            if (
                sheet?.getRow(0) == null ||
                headerColumns != expectedHeaders
            ) {
                throw ExpectedException(
                    "헤더 행의 열은 순서대로 전공동아리, 취업동아리, 창체동아리여야 합니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }
            val data = mutableListOf<ExcelRowDto>()
            val clubTypes = listOf(ClubType.MAJOR_CLUB, ClubType.JOB_CLUB, ClubType.AUTONOMOUS_CLUB)
            for (columnIndex in 0..2) {
                val clubNames = mutableListOf<String>()
                for (rowIndex in 1..sheet.lastRowNum) {
                    val cellValue = sheet.getRow(rowIndex)?.getCell(columnIndex)
                        ?.toString()
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: continue

                    clubNames.add(cellValue)
                }
                data.add(ExcelRowDto(clubName = clubNames, clubType = clubTypes[columnIndex]))
            }
            return data.toList()
        } finally {
            workbook.close()
        }
    }
}
