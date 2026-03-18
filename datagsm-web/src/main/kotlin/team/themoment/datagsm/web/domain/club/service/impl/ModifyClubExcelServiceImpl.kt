package team.themoment.datagsm.web.domain.club.service.impl

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.common.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.ModifyClubExcelService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.response.CommonApiResponse

@Service
class ModifyClubExcelServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyClubExcelService {
    companion object {
        private const val CLUB_NAME_COL_IDX = 0
        private const val CLUB_TYPE_COL_IDX = 1
        private const val LEADER_COL_IDX = 2
        private const val FOUNDED_YEAR_COL_IDX = 3
        private const val STATUS_COL_IDX = 4
        private const val ABOLISHED_YEAR_COL_IDX = 5
    }

    @Transactional
    override fun execute(file: MultipartFile): CommonApiResponse<Nothing> {
        val clubInfos: List<ClubInfoDto> = queryExcelData(file)

        if (clubInfos.isEmpty()) {
            throw ExpectedException(
                "엑셀 내 모든 동아리명이 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        val duplicates =
            clubInfos
                .groupingBy { it.clubName }
                .eachCount()
                .filter { it.value > 1 }
                .keys

        if (duplicates.isNotEmpty()) {
            throw ExpectedException(
                "엑셀 파일에 다음 동아리가 중복으로 존재합니다: $duplicates",
                HttpStatus.BAD_REQUEST,
            )
        }

        val existingClubs =
            clubJpaRepository
                .findAllByNameIn(clubInfos.map { it.clubName })
                .associateBy { it.name }

        val clubsToSave =
            clubInfos.map { dto ->
                (existingClubs[dto.clubName] ?: ClubJpaEntity()).also { club ->
                    club.name = dto.clubName
                    club.type = dto.clubType
                    club.foundedYear = dto.foundedYear
                    club.status = dto.status
                    club.abolishedYear = dto.abolishedYear
                    club.leader =
                        when (dto.status) {
                            ClubStatus.ABOLISHED -> null
                            ClubStatus.ACTIVE ->
                                dto.leaderInfo?.takeIf { it.isNotBlank() }?.let { parseAndFindLeader(it) }
                        }
                }
            }
        clubJpaRepository.saveAll(clubsToSave)

        val excelClubNames = clubInfos.map { it.clubName }
        val orphanClubs = clubJpaRepository.findByNameNotIn(excelClubNames)
        if (orphanClubs.isNotEmpty()) {
            studentJpaRepository.bulkClearClubReferences(orphanClubs)
            clubJpaRepository.deleteAllInBatch(orphanClubs)
        }

        return CommonApiResponse.success("엑셀 업로드 성공")
    }

    private fun parseAndFindLeader(leaderInfo: String): StudentJpaEntity {
        val parts = leaderInfo.trim().split(" ")
        if (parts.size != 2) {
            throw ExpectedException(
                "동아리 부장 정보 형식이 올바르지 않습니다. (형식: '학번 이름', 예: '2404 김태은')",
                HttpStatus.BAD_REQUEST,
            )
        }

        val studentNumberStr = parts[0]
        val studentName = parts[1]

        if (studentNumberStr.length != 4 || !studentNumberStr.all { it.isDigit() }) {
            throw ExpectedException(
                "학번은 4자리 숫자여야 합니다. (입력값: $studentNumberStr)",
                HttpStatus.BAD_REQUEST,
            )
        }

        val grade = studentNumberStr[0].digitToInt()
        val classNum = studentNumberStr[1].digitToInt()
        val number = studentNumberStr.substring(2).toInt()

        return studentJpaRepository
            .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                grade,
                classNum,
                number,
                studentName,
            ) ?: throw ExpectedException(
            "학번 $studentNumberStr 이름 $studentName 에 해당하는 학생을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND,
        )
    }

    private fun queryExcelData(file: MultipartFile): List<ClubInfoDto> {
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
                headerRow?.getCell(CLUB_NAME_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(CLUB_TYPE_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(LEADER_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(FOUNDED_YEAR_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(STATUS_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(ABOLISHED_YEAR_COL_IDX)?.stringCellValue ?: "",
            )
        val expectedHeaders =
            listOf(
                "동아리명",
                "동아리종류",
                "부장",
                "창설학년도",
                "운영상태",
                "폐지학년도",
            )
        workbook.use { _ ->
            if (
                sheet?.getRow(0) == null ||
                headerColumns != expectedHeaders
            ) {
                throw ExpectedException(
                    "헤더 행의 열은 순서대로 동아리명, 동아리종류, 부장, 창설학년도, 운영상태, 폐지학년도여야 합니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }

            val data =
                (1..sheet.lastRowNum).mapNotNull { rowIdx ->
                    val row = sheet.getRow(rowIdx) ?: return@mapNotNull null
                    val clubName =
                        row
                            .getCell(CLUB_NAME_COL_IDX)
                            ?.toString()
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                            ?: return@mapNotNull null

                    val typeName = row.getCell(CLUB_TYPE_COL_IDX)?.toString()?.trim() ?: ""
                    val clubType =
                        ClubType.entries.find { it.name == typeName }
                            ?: throw ExpectedException(
                                "알 수 없는 동아리 종류입니다. (입력값: $typeName)",
                                HttpStatus.BAD_REQUEST,
                            )

                    val leaderInfo = row.getCell(LEADER_COL_IDX)?.toString()?.trim()

                    val foundedYearCell = row.getCell(FOUNDED_YEAR_COL_IDX)
                    val foundedYear =
                        if (foundedYearCell == null || foundedYearCell.toString().trim().isBlank()) {
                            throw ExpectedException(
                                "창설 학년도가 비어있습니다. (행: ${rowIdx + 1})",
                                HttpStatus.BAD_REQUEST,
                            )
                        } else {
                            try {
                                foundedYearCell.numericCellValue.toInt()
                            } catch (e: IllegalStateException) {
                                throw ExpectedException(
                                    "창설 학년도는 숫자여야 합니다. (행: ${rowIdx + 1})",
                                    HttpStatus.BAD_REQUEST,
                                )
                            }
                        }

                    val statusName = row.getCell(STATUS_COL_IDX)?.toString()?.trim() ?: ""
                    val status =
                        ClubStatus.entries.find { it.name == statusName }
                            ?: throw ExpectedException(
                                "알 수 없는 운영 상태입니다. (입력값: $statusName)",
                                HttpStatus.BAD_REQUEST,
                            )

                    val abolishedYearCell = row.getCell(ABOLISHED_YEAR_COL_IDX)
                    val abolishedYear =
                        try {
                            abolishedYearCell?.numericCellValue?.toInt()
                        } catch (e: IllegalStateException) {
                            null
                        }

                    ClubInfoDto(
                        clubName = clubName,
                        clubType = clubType,
                        leaderInfo = leaderInfo,
                        foundedYear = foundedYear,
                        status = status,
                        abolishedYear = abolishedYear,
                    )
                }
            return data
        }
    }
}
