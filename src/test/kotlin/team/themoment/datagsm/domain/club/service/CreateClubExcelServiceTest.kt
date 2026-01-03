package team.themoment.datagsm.domain.club.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.impl.CreateClubExcelServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import java.io.ByteArrayInputStream

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

                        // Excel 내용 검증
                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet = workbook.getSheetAt(0)

                        sheet.sheetName shouldBe "동아리"

                        // 헤더 검증
                        val headerRow = sheet.getRow(0)
                        headerRow.getCell(0).stringCellValue shouldBe "전공동아리"
                        headerRow.getCell(1).stringCellValue shouldBe "전공동아리 부장"
                        headerRow.getCell(2).stringCellValue shouldBe "취업동아리"
                        headerRow.getCell(3).stringCellValue shouldBe "취업동아리 부장"
                        headerRow.getCell(4).stringCellValue shouldBe "창체동아리"
                        headerRow.getCell(5).stringCellValue shouldBe "창체동아리 부장"

                        // 데이터 검증
                        val dataRow = sheet.getRow(1)
                        dataRow.getCell(0).stringCellValue shouldBe "SW개발동아리"
                        dataRow.getCell(1).stringCellValue shouldBe "2404 김철수"
                        dataRow.getCell(2).stringCellValue shouldBe "취업준비동아리"
                        dataRow.getCell(3).stringCellValue shouldBe "2305 이영희"
                        dataRow.getCell(4).stringCellValue shouldBe "창체동아리A"
                        dataRow.getCell(5).stringCellValue shouldBe "1210 박민수"

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

                        // 헤더만 존재해야 함
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

                        // 헤더 + 5개 동아리
                        sheet.lastRowNum shouldBe 5

                        // 각 동아리 확인
                        for (i in 1..5) {
                            val row = sheet.getRow(i)
                            row.getCell(0).stringCellValue shouldBe "전공동아리$i"
                            row.getCell(1).stringCellValue shouldBe "210$i 학생$i"
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

                        // 헤더 + 3개 행 (취업동아리가 3개로 가장 많음)
                        sheet.lastRowNum shouldBe 3

                        // 첫 번째 행: 전공동아리1, 취업동아리1
                        val row1 = sheet.getRow(1)
                        row1.getCell(0).stringCellValue shouldBe "전공동아리1"
                        row1.getCell(2).stringCellValue shouldBe "취업동아리1"

                        // 두 번째 행: 전공동아리는 없고, 취업동아리2만
                        val row2 = sheet.getRow(2)
                        row2.getCell(0) shouldBe null
                        row2.getCell(2).stringCellValue shouldBe "취업동아리2"

                        workbook.close()
                    }
                }
            }
        }
    })
