package team.themoment.datagsm.resource.global.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class AcademicYearCalculatorTest :
    BehaviorSpec({
        given("현재 날짜가 1~2월인 경우") {
            `when`("2025년 1월 15일") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2025, 1, 15),
                    )

                then("2024학년도를 반환한다") {
                    period.academicYear shouldBe 2024
                    period.startDate shouldBe LocalDate.of(2024, 3, 1)
                    period.endDate shouldBe LocalDate.of(2025, 2, 28)
                }
            }

            `when`("2026년 2월 28일") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2026, 2, 28),
                    )

                then("2025학년도를 반환한다") {
                    period.academicYear shouldBe 2025
                    period.startDate shouldBe LocalDate.of(2025, 3, 1)
                    period.endDate shouldBe LocalDate.of(2026, 2, 28)
                }
            }
        }

        given("현재 날짜가 3~12월인 경우") {
            `when`("2025년 3월 1일") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2025, 3, 1),
                    )

                then("2025학년도를 반환한다") {
                    period.academicYear shouldBe 2025
                    period.startDate shouldBe LocalDate.of(2025, 3, 1)
                    period.endDate shouldBe LocalDate.of(2026, 2, 28)
                }
            }

            `when`("2025년 12월 31일") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2025, 12, 31),
                    )

                then("2025학년도를 반환한다") {
                    period.academicYear shouldBe 2025
                    period.startDate shouldBe LocalDate.of(2025, 3, 1)
                    period.endDate shouldBe LocalDate.of(2026, 2, 28)
                }
            }
        }

        given("윤년 처리") {
            `when`("2023년 5월 (2023학년도 종료는 윤년 2024년 2월)") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2023, 5, 1),
                    )

                then("2월 29일까지 포함") {
                    period.endDate shouldBe LocalDate.of(2024, 2, 29)
                }
            }

            `when`("2024년 5월 (2024학년도 종료는 평년 2025년 2월)") {
                val period =
                    AcademicYearCalculator.getCurrentAcademicYearPeriod(
                        LocalDate.of(2024, 5, 1),
                    )

                then("2월 28일까지 포함") {
                    period.endDate shouldBe LocalDate.of(2025, 2, 28)
                }
            }
        }
    })
