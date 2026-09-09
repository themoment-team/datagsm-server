package team.themoment.datagsm.oauth.authorization.global.security.jwt

import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.themoment.datagsm.common.global.data.OauthEnvironment
import team.themoment.datagsm.oauth.authorization.global.data.OauthJwtProvisionEnvironment
import java.security.KeyPairGenerator
import java.util.Base64

/**
 * id_token은 SP가 서명과 클레임만 보고 사용자를 식별하는 값이라,
 * 실제로 서명된 토큰을 파싱해 클레임이 들어갔는지까지 확인한다.
 * JwtProvider를 목으로 두면 nonce 누락 같은 결함이 그대로 통과한다.
 */
class JwtProviderIdTokenTest :
    DescribeSpec({

        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val encoder = Base64.getMimeEncoder()

        val privateKeyPem =
            "-----BEGIN PRIVATE KEY-----\n" +
                encoder.encodeToString(keyPair.private.encoded) +
                "\n-----END PRIVATE KEY-----"
        val publicKeyPem =
            "-----BEGIN PUBLIC KEY-----\n" +
                encoder.encodeToString(keyPair.public.encoded) +
                "\n-----END PUBLIC KEY-----"

        val jwtEnvironment =
            mockk<OauthJwtProvisionEnvironment> {
                every { privateKey } returns privateKeyPem
                every { publicKey } returns publicKeyPem
                every { keyId } returns "test-key-id"
                every { accessTokenExpiration } returns 3600000L
            }
        val oauthEnvironment =
            mockk<OauthEnvironment> {
                every { issuerUrl } returns "https://oauth.authorization.datagsm.kr"
            }

        val jwtProvider = JwtProvider(jwtEnvironment, oauthEnvironment)

        fun parse(token: String) =
            Jwts
                .parser()
                .verifyWith(keyPair.public)
                .build()
                .parseSignedClaims(token)
                .payload

        describe("JwtProvider 클래스의") {
            describe("generateIdToken 메서드는") {

                context("nonce가 주어졌을 때") {
                    it("replay 방지를 위해 nonce 클레임을 포함해야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", "n-0S6_WzA2Mj")

                        parse(token)["nonce"] shouldBe "n-0S6_WzA2Mj"
                    }
                }

                context("nonce가 없을 때") {
                    it("nonce 클레임을 넣지 않아야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", null)

                        parse(token).containsKey("nonce") shouldBe false
                    }
                }

                context("표준 클레임을 확인할 때") {
                    // sub는 email이 아닌 account.id다. email은 변경될 수 있어
                    // 바뀌는 순간 SP가 같은 사람을 다른 사용자로 인식한다.
                    it("sub는 account.id여야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", null)

                        parse(token).subject shouldBe "42"
                    }

                    it("iss와 aud가 규격대로 채워져야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", null)

                        val claims = parse(token)
                        claims.issuer shouldBe "https://oauth.authorization.datagsm.kr"
                        claims.audience shouldBe setOf("client-1")
                    }

                    it("email 클레임이 포함되어야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", null)

                        parse(token)["email"] shouldBe "user@gsm.hs.kr"
                    }

                    it("exp와 iat가 설정되어야 한다") {
                        val token = jwtProvider.generateIdToken(42L, "user@gsm.hs.kr", "client-1", null)

                        val claims = parse(token)
                        (claims.expiration != null) shouldBe true
                        (claims.issuedAt != null) shouldBe true
                    }
                }

                context("access token과 비교할 때") {
                    // access token의 sub는 기존 /userinfo 소비자와의 호환을 위해 email을 유지한다.
                    it("access token의 sub는 email로 남아 있어야 한다") {
                        val accessToken =
                            jwtProvider.generateOauthAccessToken(
                                "user@gsm.hs.kr",
                                team.themoment.datagsm.common.domain.account.entity.constant.AccountRole.USER,
                                "client-1",
                                emptySet(),
                            )

                        parse(accessToken).subject shouldBe "user@gsm.hs.kr"
                    }
                }
            }
        }
    })
