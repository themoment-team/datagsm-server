package team.themoment.datagsm.web.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubStatus
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.club.service.impl.ModifyClubExcelServiceImpl
import team.themoment.sdk.exception.ExpectedException
import java.io.ByteArrayOutputStream

private const val CLUB_NAME_COL_IDX = 0
private const val CLUB_TYPE_COL_IDX = 1
private const val LEADER_COL_IDX = 2
private const val FOUNDED_YEAR_COL_IDX = 3
private const val STATUS_COL_IDX = 4
private const val ABOLISHED_YEAR_COL_IDX = 5

class ModifyClubExcelServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var modifyClubExcelService: ModifyClubExcelService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            modifyClubExcelService = ModifyClubExcelServiceImpl(mockClubRepository, mockStudentRepository)
        }

        fun createValidExcelFile(includeAbolished: Boolean = false): ByteArray =
            ByteArrayOutputStream().use { output ->
                XSSFWorkbook().use { workbook ->
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                    headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                    headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                    headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                    headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                    headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                    val dataRow = sheet.createRow(1)
                    dataRow.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                    dataRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                    dataRow.createCell(LEADER_COL_IDX).setCellValue("2404 김철수")
                    dataRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                    dataRow.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                    dataRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                    if (includeAbolished) {
                        val abolishedRow = sheet.createRow(2)
                        abolishedRow.createCell(CLUB_NAME_COL_IDX).setCellValue("폐지된동아리")
                        abolishedRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                        abolishedRow.createCell(LEADER_COL_IDX).setCellValue("")
                        abolishedRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2020.0)
                        abolishedRow.createCell(STATUS_COL_IDX).setCellValue("ABOLISHED")
                        abolishedRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue(2023.0)
                    }

                    workbook.write(output)
                }
                output.toByteArray()
            }

        describe("ModifyClubExcelService 클래스의") {
            describe("execute 메서드는") {

                context("정상적인 Excel 파일로 동아리를 생성/수정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val leader1 =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    2,
                                    4,
                                    4,
                                    "김철수",
                                )
                        } returns leader1
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                        every { mockClubRepository.findByNameNotIn(any()) } returns emptyList()
                    }

                    it("동아리를 저장하고 성공 메시지를 반환해야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"
                        result.code shouldBe HttpStatus.OK.value()

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs.size shouldBe 1
                        savedClubs[0].name shouldBe "SW개발동아리"
                        savedClubs[0].type shouldBe ClubType.MAJOR_CLUB
                        savedClubs[0].leader?.name shouldBe "김철수"
                        savedClubs[0].foundedYear shouldBe 2022
                        savedClubs[0].status shouldBe ClubStatus.ACTIVE
                    }
                }

                context("ABOLISHED 동아리가 포함된 Excel을 업로드할 때") {
                    val excelBytes = createValidExcelFile(includeAbolished = true)
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val leader1 =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    2,
                                    4,
                                    4,
                                    "김철수",
                                )
                        } returns leader1
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                        every { mockClubRepository.findByNameNotIn(any()) } returns emptyList()
                    }

                    it("ABOLISHED 동아리는 leader=null로 저장되고 findByStudentNumber가 호출되지 않아야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs.size shouldBe 2
                        val abolishedClub = savedClubs.find { it.name == "폐지된동아리" }!!
                        abolishedClub.leader shouldBe null
                        abolishedClub.status shouldBe ClubStatus.ABOLISHED
                        abolishedClub.abolishedYear shouldBe 2023
                    }
                }

                context("지원하지 않는 파일 형식일 때") {
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.pdf",
                            "application/pdf",
                            "dummy pdf content".toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldBe "지원하지 않는 파일 형식입니다. (xlsx, xls만 지원)"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("중복된 동아리명이 존재할 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("중복동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("2404 김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                val row2 = sheet.createRow(2)
                                row2.createCell(CLUB_NAME_COL_IDX).setCellValue("중복동아리")
                                row2.createCell(CLUB_TYPE_COL_IDX).setCellValue("AUTONOMOUS_CLUB")
                                row2.createCell(LEADER_COL_IDX).setCellValue("1210 박민수")
                                row2.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row2.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row2.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )
                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "엑셀 파일에 다음 동아리가 중복으로 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("모든 동아리명이 비어있을 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldBe "엑셀 내 모든 동아리명이 비어있습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 헤더 형식일 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("잘못된헤더")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "헤더 행의 열은 순서대로"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("알 수 없는 동아리종류일 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("UNKNOWN_TYPE")
                                row1.createCell(LEADER_COL_IDX).setCellValue("2404 김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "알 수 없는 동아리 종류입니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("알 수 없는 운영상태일 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("2404 김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("UNKNOWN_STATUS")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "알 수 없는 운영 상태입니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("창설학년도가 비어있을 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("2404 김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("")
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "창설 학년도가 비어있습니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("ACTIVE인데 부장 정보가 비어있을 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                        every { mockClubRepository.findByNameNotIn(any()) } returns emptyList()
                    }

                    it("leader=null로 저장되어야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs[0].leader shouldBe null
                        savedClubs[0].status shouldBe ClubStatus.ACTIVE
                    }
                }

                context("부장 정보 형식이 올바르지 않을 때 (스페이스 누락)") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("2404김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "동아리 부장 정보 형식이 올바르지 않습니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("학번이 4자리가 아닐 때") {
                    val excelBytes =
                        ByteArrayOutputStream().use { output ->
                            XSSFWorkbook().use { workbook ->
                                val sheet = workbook.createSheet("동아리")
                                val headerRow = sheet.createRow(0)
                                headerRow.createCell(CLUB_NAME_COL_IDX).setCellValue("동아리명")
                                headerRow.createCell(CLUB_TYPE_COL_IDX).setCellValue("동아리종류")
                                headerRow.createCell(LEADER_COL_IDX).setCellValue("부장")
                                headerRow.createCell(FOUNDED_YEAR_COL_IDX).setCellValue("창설학년도")
                                headerRow.createCell(STATUS_COL_IDX).setCellValue("운영상태")
                                headerRow.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("폐지학년도")

                                val row1 = sheet.createRow(1)
                                row1.createCell(CLUB_NAME_COL_IDX).setCellValue("SW개발동아리")
                                row1.createCell(CLUB_TYPE_COL_IDX).setCellValue("MAJOR_CLUB")
                                row1.createCell(LEADER_COL_IDX).setCellValue("240 김철수")
                                row1.createCell(FOUNDED_YEAR_COL_IDX).setCellValue(2022.0)
                                row1.createCell(STATUS_COL_IDX).setCellValue("ACTIVE")
                                row1.createCell(ABOLISHED_YEAR_COL_IDX).setCellValue("")

                                workbook.write(output)
                            }
                            output.toByteArray()
                        }

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "학번은 4자리 숫자여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("존재하지 않는 학생으로 부장을 지정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                )
                        } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "에 해당하는 학생을 찾을 수 없습니다"
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }

                context("기존 동아리를 수정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val existingClub =
                        ClubJpaEntity().apply {
                            id = 100L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                            foundedYear = 2022
                            status = ClubStatus.ACTIVE
                        }

                    val newLeader =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns listOf(existingClub)
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                )
                        } returns newLeader
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                        every { mockClubRepository.findByNameNotIn(any()) } returns emptyList()
                    }

                    it("기존 동아리를 수정해야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs.any { it.id == 100L } shouldBe true
                    }
                }

                context("DB에만 있고 엑셀에 없는 동아리가 있을 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val orphanClub =
                        ClubJpaEntity().apply {
                            id = 999L
                            name = "삭제될동아리"
                            type = ClubType.MAJOR_CLUB
                            foundedYear = 2022
                            status = ClubStatus.ACTIVE
                        }

                    val leader1 =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                )
                        } returns leader1
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                        every { mockClubRepository.findByNameNotIn(any()) } returns listOf(orphanClub)
                        every { mockStudentRepository.bulkClearClubReferences(any()) } just Runs
                        every { mockClubRepository.deleteAllInBatch(any<Iterable<ClubJpaEntity>>()) } just Runs
                    }

                    it("DB에만 있는 동아리를 삭제해야 한다") {
                        modifyClubExcelService.execute(file)

                        verify(exactly = 1) {
                            mockStudentRepository.bulkClearClubReferences(
                                match { clubs -> clubs.any { it.id == 999L } },
                            )
                        }
                        verify(exactly = 1) {
                            mockClubRepository.deleteAllInBatch(
                                match<Iterable<ClubJpaEntity>> { clubs ->
                                    clubs.toList().any { it.id == 999L }
                                },
                            )
                        }
                    }
                }
            }
        }
    })
