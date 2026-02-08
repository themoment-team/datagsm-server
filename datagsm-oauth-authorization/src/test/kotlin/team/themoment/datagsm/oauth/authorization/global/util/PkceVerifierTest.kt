package team.themoment.datagsm.oauth.authorization.global.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import team.themoment.datagsm.common.domain.oauth.entity.constant.PkceChallengeMethod
import java.security.MessageDigest
import java.util.Base64

class PkceVerifierTest :
    DescribeSpec({

        describe("PkceVerifier 클래스의") {
            describe("verify 메서드는") {

                context("S256 방식의 PKCE 검증 시") {
                    val verifier = "dBjftJeSSVPxgS31dKTHlEpQMZlzvvMpqHN0KT9LM5E"
                    val hash = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
                    val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash)

                    it("올바른 verifier를 제공하면 true를 반환한다") {
                        val result = PkceVerifier.verify(challenge, PkceChallengeMethod.S256, verifier)

                        result shouldBe true
                    }

                    it("잘못된 verifier를 제공하면 false를 반환한다") {
                        val result = PkceVerifier.verify(challenge, PkceChallengeMethod.S256, "wrong-verifier")

                        result shouldBe false
                    }
                }

                context("plain 방식의 PKCE 검증 시") {
                    val verifier = "plain-code-verifier"
                    val challenge = verifier

                    it("올바른 verifier를 제공하면 true를 반환한다") {
                        val result = PkceVerifier.verify(challenge, PkceChallengeMethod.PLAIN, verifier)

                        result shouldBe true
                    }

                    it("잘못된 verifier를 제공하면 false를 반환한다") {
                        val result = PkceVerifier.verify(challenge, PkceChallengeMethod.PLAIN, "wrong-verifier")

                        result shouldBe false
                    }
                }
            }
        }
    })
