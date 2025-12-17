package team.themoment.datagsm.domain.club.service.impl

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.domain.club.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
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

    override fun execute(file: MultipartFile): CommonApiResponse<Nothing> {
        val excelData: List<ExcelRowDto> = queryExcelData(file)
        val clubNames =
            excelData.flatMap { row ->
                row.clubName.map { name ->
                    ClubInfoDto(
                        clubName = name,
                        clubType = row.clubType,
                    )
                }
            }

        if (clubNames.isEmpty()) {
            throw ExpectedException(
                "엑셀 내 모든 동아리명이 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        val duplicates = clubNames.groupingBy { it.clubName }.eachCount()
            .filter { it.value > 1 }.keys

        if (duplicates.isNotEmpty()) {
            throw ExpectedException(
                "엑셀 파일에 다음 동아리가 중복으로 존재합니다: $duplicates",
                HttpStatus.BAD_REQUEST
            )
        }

        val existingClubs =
            clubJpaRepository
                .findAllByNameIn(clubNames.map { it.clubName })
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

    private fun queryExcelData(file: MultipartFile): List<ExcelRowDto> {
        val workbook =
            file.inputStream.use { inputStream ->
                when (file.originalFilename?.substringAfterLast(".")) {
                    "xlsx" -> XSSFWorkbook(inputStream)
                    "xls" -> HSSFWorkbook(inputStream)
                    else -> throw ExpectedException("지원하지 않는 파일 형식입니다. (xlsx, xls만 지원)", HttpStatus.BAD_REQUEST)
                }
            }

        val sheet = workbook.getSheetAt(0)
        val headerRow = sheet?.getRow(0)
        val headerColumns =
            listOf(
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
            val headerToClubType = ClubType.entries.associateBy { it.value }
            val data =
                headerColumns.mapIndexed { colIdx, header ->
                    val clubType = headerToClubType[header]!!
                    val clubNames =
                        (1..sheet.lastRowNum).mapNotNull { rowIdx ->
                            sheet
                                .getRow(rowIdx)
                                ?.getCell(colIdx)
                                ?.toString()
                                ?.trim()
                                ?.takeIf { it.isNotBlank() }
                        }
                    ExcelRowDto(clubName = clubNames, clubType = clubType)
                }
            return data
        } finally {
            workbook.close()
        }
    }
}
