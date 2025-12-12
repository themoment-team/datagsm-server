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

        try {
            if (
                !(sheet?.getRow(0)?.getCell(0)?.stringCellValue?.equals("전공동아리") ?: true) ||
                !(sheet?.getRow(0)?.getCell(1)?.stringCellValue?.equals("직업동아리") ?: true) ||
                !(sheet?.getRow(0)?.getCell(2)?.stringCellValue?.equals("창체동아리") ?: true)
            ) {
                throw ExpectedException(
                    "헤더 행의 열은 순서대로 전공동아리, 직업동아리, 창체동아리여야 합니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }
            val data = mutableListOf<ExcelRowDto>()
            val clubTypes = listOf(ClubType.MAJOR_CLUB, ClubType.JOB_CLUB, ClubType.AUTONOMOUS_CLUB)
            for(columnIndex in 0..2) {
                val clubNames = mutableListOf<String>()
                for(rowIndex in 1..sheet.lastRowNum) {
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
