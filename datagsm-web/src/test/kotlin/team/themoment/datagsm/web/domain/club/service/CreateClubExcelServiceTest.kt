package team.themoment.datagsm.web.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.club.service.impl.CreateClubExcelServiceImpl
import java.io.ByteArrayInputStream

private const val CLUB_NAME_COL_IDX = 0
private const val CLUB_TYPE_COL_IDX = 1
private const val LEADER_COL_IDX = 2
private const val FOUNDED_YEAR_COL_IDX = 3
private const val STATUS_COL_IDX = 4
private const val ABOLISHED_YEAR_COL_IDX = 5

class CreateClubExcelServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var createClubExcelService: CreateClubExcelService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            createClubExcelService = CreateClubExcelServiceImpl(mockClubRepository)
        }

        describe("CreateClubExcelService 클래스의") {
            describe("execute 메서드는") {

                context("정상적인 동아리 데이터로 Excel을 생성할 때") {
                    val majorClubLeader =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "major@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    val autonomousClubLeader =
                        StudentJpaEntity().apply {
                            id = 3L
                            name = "박민수"
                            studentNumber = StudentNumber(1, 2, 10)
                            email = "auto@gsm.hs.kr"
                            major = Major.SMART_IOT
                            sex = Sex.MAN
                        }

                    val clubs =
                        listOf(
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "SW개발동아리"
                                type = ClubType.MAJOR_CLUB
                                leader = majorClubLeader
                                foundedYear = 2022
                                status = ClubStatus.ACTIVE
                            },
                            ClubJpaEntity().apply {
                                id = 3L
                                name = "창체동아리A"
                                type = ClubType.AUTONOMOUS_CLUB
                                leader = autonomousClubLeader
                                foundedYear = 2021
                                status = ClubStatus.ACTIVE
                            },
                        )

                    beforeEach {
                        every { mockClubRepository.findAll() } returns clubs
                    }

                    it("Excel 파일을 생성하고 올바른 ResponseEntity를 반환해야 한다") {
                        val result = createClubExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK
                        result.body shouldNotBe null
                        result.headers.contentType.toString() shouldContain
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        result.headers.contentDisposition.filename shouldMatch Regex("동아리_현황_\\d{8}\\.xlsx")

                        verify(exactly = 1) { mockClubRepository.findAll() }

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)

                        sheet.sheetName shouldBe "동아리"

                        val headerRow = sheet.getRow(0)
                        headerRow.getCell(CLUB_NAME_COL_IDX).stringCellValue shouldBe "동아리명"
                        headerRow.getCell(CLUB_TYPE_COL_IDX).stringCellValue shouldBe "동아리종류"
                        headerRow.getCell(LEADER_COL_IDX).stringCellValue shouldBe "부장"
                        headerRow.getCell(FOUNDED_YEAR_COL_IDX).stringCellValue shouldBe "창설학년도"
                        headerRow.getCell(STATUS_COL_IDX).stringCellValue shouldBe "운영상태"
                        headerRow.getCell(ABOLISHED_YEAR_COL_IDX).stringCellValue shouldBe "폐지학년도"

                        val dataRow1 = sheet.getRow(1)
                        dataRow1.getCell(CLUB_NAME_COL_IDX).stringCellValue shouldBe "SW개발동아리"
                        dataRow1.getCell(CLUB_TYPE_COL_IDX).stringCellValue shouldBe "MAJOR_CLUB"
                        dataRow1.getCell(LEADER_COL_IDX).stringCellValue shouldBe "2404 김철수"
                        dataRow1.getCell(FOUNDED_YEAR_COL_IDX).numericCellValue shouldBe 2022.0
                        dataRow1.getCell(STATUS_COL_IDX).stringCellValue shouldBe "ACTIVE"
                        dataRow1.getCell(ABOLISHED_YEAR_COL_IDX).stringCellValue shouldBe ""

                        workbook.close()
                    }
                }

                context("빈 동아리 리스트로 Excel을 생성할 때") {
                    beforeEach {
                        every { mockClubRepository.findAll() } returns emptyList()
                    }

                    it("헤더만 있는 Excel 파일을 생성해야 한다") {
                        val result = createClubExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK
                        result.body shouldNotBe null

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)
                        sheet.lastRowNum shouldBe 0

                        workbook.close()
                    }
                }

                context("ABOLISHED 동아리가 포함될 때") {
                    val abolishedClub =
                        ClubJpaEntity().apply {
                            id = 5L
                            name = "폐지된동아리"
                            type = ClubType.MAJOR_CLUB
                            leader = null
                            foundedYear = 2020
                            status = ClubStatus.ABOLISHED
                            abolishedYear = 2023
                        }

                    beforeEach {
                        every { mockClubRepository.findAll() } returns listOf(abolishedClub)
                    }

                    it("부장 셀이 blank이고 폐지학년도 셀이 채워져야 한다") {
                        val result = createClubExcelService.execute()

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)
                        val dataRow = sheet.getRow(1)

                        dataRow.getCell(CLUB_NAME_COL_IDX).stringCellValue shouldBe "폐지된동아리"
                        dataRow.getCell(LEADER_COL_IDX).stringCellValue shouldBe ""
                        dataRow.getCell(STATUS_COL_IDX).stringCellValue shouldBe "ABOLISHED"
                        dataRow.getCell(ABOLISHED_YEAR_COL_IDX).numericCellValue shouldBe 2023.0

                        workbook.close()
                    }
                }

                context("다수의 동아리가 있을 때") {
                    val clubs =
                        (1..5).map { idx ->
                            val leader =
                                StudentJpaEntity().apply {
                                    id = idx.toLong()
                                    name = "학생$idx"
                                    studentNumber = StudentNumber(2, 1, idx)
                                    email = "student$idx@gsm.hs.kr"
                                    major = Major.SW_DEVELOPMENT
                                    sex = Sex.MAN
                                }
                            ClubJpaEntity().apply {
                                id = idx.toLong()
                                name = "전공동아리$idx"
                                type = ClubType.MAJOR_CLUB
                                this.leader = leader
                                foundedYear = 2022
                                status = ClubStatus.ACTIVE
                            }
                        }

                    beforeEach {
                        every { mockClubRepository.findAll() } returns clubs
                    }

                    it("모든 동아리가 포함된 Excel 파일을 생성해야 한다") {
                        val result = createClubExcelService.execute()
                        result.statusCode shouldBe HttpStatus.OK
                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)
                        sheet.lastRowNum shouldBe 5
                        for (i in 1..5) {
                            val row = sheet.getRow(i)
                            row.getCell(CLUB_NAME_COL_IDX).stringCellValue shouldBe "전공동아리$i"
                            row.getCell(CLUB_TYPE_COL_IDX).stringCellValue shouldBe "MAJOR_CLUB"
                        }

                        workbook.close()
                    }
                }
            }
        }
    })
