package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.event.dto.internal.EventDispatchRequested
import team.themoment.datagsm.common.domain.event.dto.payload.EventChangedData
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.event.entity.constant.EventType
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMySpecialtyReqDto
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class ModifyMySpecialtyServiceImplTest :
    BehaviorSpec({
        val currentUserProvider = mockk<CurrentUserProvider>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val eventSlot = slot<EventDispatchRequested>()
        val service = ModifyMySpecialtyServiceImpl(currentUserProvider, applicationEventPublisher)
        justRun { applicationEventPublisher.publishEvent(capture(eventSlot)) }

        Given("학생이 연결된 계정으로 전공을 수정할 때") {
            val student =
                StudentJpaEntity().apply {
                    id = 1L
                    name = "홍길동"
                    email = "hong@gsm.hs.kr"
                    sex = Sex.MAN
                }
            val reqDto = UpdateMySpecialtyReqDto(specialty = "백엔드")

            every { currentUserProvider.getCurrentStudent() } returns student

            When("서비스를 실행하면") {
                service.execute(reqDto)

                Then("학생의 specialty가 변경된다") {
                    student.specialty shouldBe "백엔드"
                }

                Then("STUDENT_UPDATED 이벤트가 1회 발행되며 old/new에 변경 전후가 담긴다") {
                    verify(exactly = 1) {
                        applicationEventPublisher.publishEvent(
                            match<EventDispatchRequested> { it.eventType == EventType.STUDENT_UPDATED },
                        )
                    }
                    val data = eventSlot.captured.data as EventChangedData
                    data.old.size shouldBe 1
                    data.new.size shouldBe 1
                    (data.old[0].obj as StudentEventObject).specialty shouldBe null
                    (data.new[0].obj as StudentEventObject).specialty shouldBe "백엔드"
                }
            }
        }

        Given("기존에 전공이 설정된 학생이 null로 삭제를 요청할 때") {
            val student =
                StudentJpaEntity().apply {
                    id = 2L
                    name = "김학생"
                    email = "kim@gsm.hs.kr"
                    sex = Sex.WOMAN
                    specialty = "프론트엔드"
                }
            val reqDto = UpdateMySpecialtyReqDto(specialty = null)

            every { currentUserProvider.getCurrentStudent() } returns student

            When("서비스를 실행하면") {
                service.execute(reqDto)

                Then("학생의 specialty가 null로 변경된다") {
                    student.specialty shouldBe null
                }
            }
        }

        Given("학생 정보가 연결되지 않은 계정으로 전공 수정을 시도할 때") {
            val reqDto = UpdateMySpecialtyReqDto(specialty = "백엔드")

            every { currentUserProvider.getCurrentStudent() } throws
                ExpectedException("학생 정보가 연결되지 않은 계정입니다.", HttpStatus.FORBIDDEN)

            When("서비스를 실행하면") {
                Then("ExpectedException이 발생한다") {
                    val exception =
                        shouldThrow<ExpectedException> {
                            service.execute(reqDto)
                        }

                    exception.message shouldBe "학생 정보가 연결되지 않은 계정입니다."
                }
            }
        }
    })
