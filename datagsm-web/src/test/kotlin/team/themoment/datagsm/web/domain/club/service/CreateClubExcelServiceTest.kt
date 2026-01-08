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
import team.themoment.datagsm.common.domain.club.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.Major
import team.themoment.datagsm.common.domain.student.Sex
import team.themoment.datagsm.common.domain.student.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.StudentNumber
import team.themoment.datagsm.web.domain.club.service.impl.CreateClubExcelServiceImpl
import java.io.ByteArrayInputStream

private const val MAJOR_CLUB_COL_IDX = 0
private const val MAJOR_CLUB_LEADER_COL_IDX = 1
private const val JOB_CLUB_COL_IDX = 2
private const val JOB_CLUB_LEADER_COL_IDX = 3
private const val AUTONOMOUS_CLUB_COL_IDX = 4
private const val AUTONOMOUS_CLUB_LEADER_COL_IDX = 5

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

                    val jobClubLeader =
                        StudentJpaEntity().apply {
                            id = 2L
                            name = "이영희"
                            studentNumber = StudentNumber(2, 3, 5)
                            email = "job@gsm.hs.kr"
                            major = Major.AI
                            sex = Sex.WOMAN
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

                    val majorClubs =
                        listOf(
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "SW개발동아리"
                                type = ClubType.MAJOR_CLUB
                                leader = majorClubLeader
                            },
                        )

                    val jobClubs =
                        listOf(
                            ClubJpaEntity().apply {
                                id = 2L
                                name = "취업준비동아리"
                                type = ClubType.JOB_CLUB
                                leader = jobClubLeader
                            },
                        )

                    val autonomousClubs =
                        listOf(
                            ClubJpaEntity().apply {
                                id = 3L
                                name = "창체동아리A"
                                type = ClubType.AUTONOMOUS_CLUB
                                leader = autonomousClubLeader
                            },
                        )

                    beforeEach {
                        every { mockClubRepository.findByType(ClubType.MAJOR_CLUB) } returns majorClubs
                        every { mockClubRepository.findByType(ClubType.JOB_CLUB) } returns jobClubs
                        every { mockClubRepository.findByType(ClubType.AUTONOMOUS_CLUB) } returns autonomousClubs
                    }

                    it("Excel 파일을 생성하고 올바른 ResponseEntity를 반환해야 한다") {
                        val result = createClubExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK
                        result.body shouldNotBe null
                        result.headers.contentType.toString() shouldContain
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        result.headers.contentDisposition.filename shouldMatch Regex("동아리_현황_\\d{8}\\.xlsx")

                        verify(exactly = 1) { mockClubRepository.findByType(ClubType.MAJOR_CLUB) }
                        verify(exactly = 1) { mockClubRepository.findByType(ClubType.JOB_CLUB) }
                        verify(exactly = 1) { mockClubRepository.findByType(ClubType.AUTONOMOUS_CLUB) }

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)

                        sheet.sheetName shouldBe "동아리"

                        val headerRow = sheet.getRow(0)
                        headerRow.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "전공동아리"
                        headerRow.getCell(MAJOR_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "전공동아리 부장"
                        headerRow.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업동아리"
                        headerRow.getCell(JOB_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "취업동아리 부장"
                        headerRow.getCell(AUTONOMOUS_CLUB_COL_IDX).stringCellValue shouldBe "창체동아리"
                        headerRow.getCell(AUTONOMOUS_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "창체동아리 부장"

                        val dataRow = sheet.getRow(1)
                        dataRow.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "SW개발동아리"
                        dataRow.getCell(MAJOR_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "2404 김철수"
                        dataRow.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업준비동아리"
                        dataRow.getCell(JOB_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "2305 이영희"
                        dataRow.getCell(AUTONOMOUS_CLUB_COL_IDX).stringCellValue shouldBe "창체동아리A"
                        dataRow.getCell(AUTONOMOUS_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "1210 박민수"

                        workbook.close()
                    }
                }

                context("빈 동아리 리스트로 Excel을 생성할 때") {
                    beforeEach {
                        every { mockClubRepository.findByType(ClubType.MAJOR_CLUB) } returns emptyList()
                        every { mockClubRepository.findByType(ClubType.JOB_CLUB) } returns emptyList()
                        every { mockClubRepository.findByType(ClubType.AUTONOMOUS_CLUB) } returns emptyList()
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

                context("다수의 동아리가 있을 때") {
                    val leaders =
                        (1..5).map { idx ->
                            StudentJpaEntity().apply {
                                id = idx.toLong()
                                name = "학생$idx"
                                studentNumber = StudentNumber(2, 1, idx)
                                email = "student$idx@gsm.hs.kr"
                                major = Major.SW_DEVELOPMENT
                                sex = Sex.MAN
                            }
                        }

                    val majorClubs =
                        leaders.mapIndexed { idx, leader ->
                            ClubJpaEntity().apply {
                                id = idx.toLong() + 1
                                name = "전공동아리${idx + 1}"
                                type = ClubType.MAJOR_CLUB
                                this.leader = leader
                            }
                        }

                    beforeEach {
                        every { mockClubRepository.findByType(ClubType.MAJOR_CLUB) } returns majorClubs
                        every { mockClubRepository.findByType(ClubType.JOB_CLUB) } returns emptyList()
                        every { mockClubRepository.findByType(ClubType.AUTONOMOUS_CLUB) } returns emptyList()
                    }

                    it("모든 동아리가 포함된 Excel 파일을 생성해야 한다") {
                        val result = createClubExcelService.execute()
                        result.statusCode shouldBe HttpStatus.OK
                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)
                        sheet.lastRowNum shouldBe 5
                        for (i in 1..5) {
                            val row = sheet.getRow(i)
                            row.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "전공동아리$i"
                            row.getCell(MAJOR_CLUB_LEADER_COL_IDX).stringCellValue shouldBe "21${String.format("%02d", i)} 학생$i"
                        }

                        workbook.close()
                    }
                }

                context("각 타입별로 다른 개수의 동아리가 있을 때") {
                    val majorLeader =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "전공부장"
                            studentNumber = StudentNumber(2, 1, 1)
                            email = "major@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    val jobLeaders =
                        (1..3).map { idx ->
                            StudentJpaEntity().apply {
                                id = idx.toLong() + 1
                                name = "취업부장$idx"
                                studentNumber = StudentNumber(2, 2, idx)
                                email = "job$idx@gsm.hs.kr"
                                major = Major.AI
                                sex = Sex.WOMAN
                            }
                        }

                    val majorClubs =
                        listOf(
                            ClubJpaEntity().apply {
                                id = 1L
                                name = "전공동아리1"
                                type = ClubType.MAJOR_CLUB
                                leader = majorLeader
                            },
                        )

                    val jobClubs =
                        jobLeaders.mapIndexed { idx, leader ->
                            ClubJpaEntity().apply {
                                id = idx.toLong() + 2
                                name = "취업동아리${idx + 1}"
                                type = ClubType.JOB_CLUB
                                this.leader = leader
                            }
                        }

                    beforeEach {
                        every { mockClubRepository.findByType(ClubType.MAJOR_CLUB) } returns majorClubs
                        every { mockClubRepository.findByType(ClubType.JOB_CLUB) } returns jobClubs
                        every { mockClubRepository.findByType(ClubType.AUTONOMOUS_CLUB) } returns emptyList()
                    }

                    it("가장 많은 동아리 개수만큼 행을 생성해야 한다") {
                        val result = createClubExcelService.execute()
                        result.statusCode shouldBe HttpStatus.OK
                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)
                        sheet.lastRowNum shouldBe 3
                        val row1 = sheet.getRow(1)
                        row1.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "전공동아리1"
                        row1.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업동아리1"
                        val row2 = sheet.getRow(2)
                        row2.getCell(MAJOR_CLUB_COL_IDX) shouldBe null
                        row2.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업동아리2"
                        workbook.close()
                    }
                }
            }
        }
    })
