package team.themoment.datagsm.web.domain.club.service.impl

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.web.domain.club.dto.internal.ClubInfoDto
import team.themoment.datagsm.web.domain.club.dto.internal.ExcelRowDto
import team.themoment.datagsm.web.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.web.domain.club.service.ModifyClubExcelService
import team.themoment.datagsm.web.domain.student.repository.StudentJpaRepository
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.response.CommonApiResponse

@Service
@Transactional
class ModifyClubExcelServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyClubExcelService {
    companion object {
        private const val MAJOR_CLUB_COL_IDX = 0
        private const val MAJOR_CLUB_LEADER_COL_IDX = 1
        private const val JOB_CLUB_COL_IDX = 2
        private const val JOB_CLUB_LEADER_COL_IDX = 3
        private const val AUTONOMOUS_CLUB_COL_IDX = 4
        private const val AUTONOMOUS_CLUB_LEADER_COL_IDX = 5
    }

    override fun execute(file: MultipartFile): CommonApiResponse<Nothing> {
        val excelData: List<ExcelRowDto> = queryExcelData(file)
        val clubInfos =
            excelData.flatMap { row ->
                row.clubName.zip(row.clubLeader).map { (name, leader) ->
                    ClubInfoDto(
                        clubName = name,
                        clubType = row.clubType,
                        leaderInfo = leader,
                    )
                }
            }

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
                    club.leader = parseAndFindLeader(dto.leaderInfo)
                }
            }
        clubJpaRepository.saveAll(clubsToSave)
        return CommonApiResponse.success("엑셀 업로드 성공")
    }

    private fun parseAndFindLeader(leaderInfo: String?): StudentJpaEntity {
        if (leaderInfo.isNullOrBlank()) {
            throw ExpectedException(
                "동아리 부장 정보가 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

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
                headerRow?.getCell(MAJOR_CLUB_LEADER_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(JOB_CLUB_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(JOB_CLUB_LEADER_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(AUTONOMOUS_CLUB_COL_IDX)?.stringCellValue ?: "",
                headerRow?.getCell(AUTONOMOUS_CLUB_LEADER_COL_IDX)?.stringCellValue ?: "",
            )
        val expectedHeaders =
            listOf(
                "전공동아리",
                "전공동아리 부장",
                "취업동아리",
                "취업동아리 부장",
                "창체동아리",
                "창체동아리 부장",
            )
        try {
            if (
                sheet?.getRow(0) == null ||
                headerColumns != expectedHeaders
            ) {
                throw ExpectedException(
                    "헤더 행의 열은 순서대로 전공동아리, 전공동아리 부장, 취업동아리, 취업동아리 부장, 창체동아리, 창체동아리 부장여야 합니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }
            val headerToClubType = ClubType.entries.associateBy { it.value }
            val data =
                headerColumns
                    .filterIndexed { index, _ -> index % 2 == 0 }
                    .mapIndexed { idx, header ->
                        val clubType = headerToClubType[header]!!
                        val clubNameColIdx = idx * 2
                        val clubLeaderColIdx = idx * 2 + 1
                        val clubAndLeaderPairs =
                            (1..sheet.lastRowNum).mapNotNull { rowIdx ->
                                val row = sheet.getRow(rowIdx)
                                val clubName =
                                    row
                                        ?.getCell(clubNameColIdx)
                                        ?.toString()
                                        ?.trim()
                                        ?.takeIf { it.isNotBlank() }

                                clubName?.let {
                                    val clubLeader =
                                        row.getCell(clubLeaderColIdx)?.toString()?.trim() ?: ""
                                    it to clubLeader
                                }
                            }
                        val clubNames = clubAndLeaderPairs.map { it.first }
                        val clubLeaders = clubAndLeaderPairs.map { it.second }
                        ExcelRowDto(clubName = clubNames, clubLeader = clubLeaders, clubType = clubType)
                    }
            return data
        } finally {
            workbook.close()
        }
    }
}
