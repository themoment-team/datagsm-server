package team.themoment.datagsm.web.domain.student.service.impl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

class ModifyMyGithubIdServiceImplTest :
    BehaviorSpec({
        val currentUserProvider = mockk<CurrentUserProvider>()
        val service = ModifyMyGithubIdServiceImpl(currentUserProvider)

        Given("학생이 연결된 계정으로 GitHub ID를 수정할 때") {
            val student =
                StudentJpaEntity().apply {
                    id = 1L
                    name = "홍길동"
                    email = "hong@gsm.hs.kr"
                    sex = Sex.MAN
                }
            val account =
                AccountJpaEntity().apply {
                    id = 10L
                    email = "hong@gsm.hs.kr"
                    password = "password"
                    this.student = student
                }
            val reqDto = UpdateMyGithubIdReqDto(githubId = "torvalds")

            every { currentUserProvider.getCurrentAccount() } returns account

            When("서비스를 실행하면") {
                service.execute(reqDto)

                Then("학생의 githubId가 변경된다") {
                    student.githubId shouldBe "torvalds"
                }
            }
        }

        Given("기존에 GitHub ID가 설정된 학생이 null로 삭제를 요청할 때") {
            val student =
                StudentJpaEntity().apply {
                    id = 2L
                    name = "김학생"
                    email = "kim@gsm.hs.kr"
                    sex = Sex.WOMAN
                    githubId = "existing-user"
                }
            val account =
                AccountJpaEntity().apply {
                    id = 20L
                    email = "kim@gsm.hs.kr"
                    password = "password"
                    this.student = student
                }
            val reqDto = UpdateMyGithubIdReqDto(githubId = null)

            every { currentUserProvider.getCurrentAccount() } returns account

            When("서비스를 실행하면") {
                service.execute(reqDto)

                Then("학생의 githubId가 null로 변경된다") {
                    student.githubId shouldBe null
                }
            }
        }

        Given("학생 정보가 연결되지 않은 계정으로 GitHub ID 수정을 시도할 때") {
            val account =
                AccountJpaEntity().apply {
                    id = 30L
                    email = "nonstudent@gsm.hs.kr"
                    password = "password"
                    student = null
                }
            val reqDto = UpdateMyGithubIdReqDto(githubId = "torvalds")

            every { currentUserProvider.getCurrentAccount() } returns account

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
