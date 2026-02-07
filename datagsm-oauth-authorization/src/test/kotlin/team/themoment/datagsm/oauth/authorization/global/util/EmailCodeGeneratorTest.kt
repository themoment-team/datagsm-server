package team.themoment.datagsm.oauth.authorization.global.util

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EmailCodeGeneratorTest :
    BehaviorSpec({
        Given("EmailCodeGenerator를 사용하여") {
            When("인증 코드를 생성하면") {
                val code = EmailCodeGenerator.generate()

                Then("8자리 숫자 코드가 생성된다") {
                    code.length shouldBe 8
                    code.all { it.isDigit() } shouldBe true
                }

                Then("코드는 0으로 패딩되어야 한다") {
                    code.toIntOrNull() shouldNotBe null
                }
            }

            When("여러 번 코드를 생성하면") {
                val codes = List(100) { EmailCodeGenerator.generate() }

                Then("모든 코드는 8자리여야 한다") {
                    codes.all { it.length == 8 } shouldBe true
                }

                Then("코드들은 서로 달라야 한다 (높은 확률로)") {
                    codes.distinct().size shouldNotBe 1
                }
            }
        }
    })
