package team.themoment.datagsm.common.domain.webhook.validator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WebhookUrlValidatorTest :
    DescribeSpec({
        describe("WebhookUrlValidator 클래스의") {
            describe("isPrivateOrLocalUrl 메서드는") {
                context("유효한 외부 HTTPS URL이 주어질 때") {
                    it("false를 반환해야 한다") {
                        // Given
                        val url = "https://example.com/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe false
                    }
                }

                context("유효한 외부 HTTP URL이 주어질 때") {
                    it("false를 반환해야 한다") {
                        // Given
                        val url = "http://example.com/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe false
                    }
                }

                context("localhost URL이 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://localhost/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("포트가 포함된 localhost URL이 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "https://localhost:8080/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("루프백 IP(127.0.0.1)가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://127.0.0.1/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("포트가 포함된 루프백 IP(127.0.0.1:8080)가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://127.0.0.1:8080/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("10.x.x.x 대역 IP가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://10.0.0.1/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("172.16.x.x 대역 IP가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://172.16.0.1/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("192.168.x.x 대역 IP가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://192.168.1.1/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("링크-로컬 IP(169.254.169.254)가 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://169.254.169.254/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("IPv6 루프백(::1)이 주어질 때") {
                    it("true를 반환해야 한다") {
                        // Given
                        val url = "http://[::1]/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe true
                    }
                }

                context("존재하지 않는 도메인이 주어질 때") {
                    it("false를 반환해야 한다") {
                        // Given
                        val url = "http://this-domain-does-not-exist-datagsm.invalid/hook"

                        // When
                        val result = WebhookUrlValidator.isPrivateOrLocalUrl(url)

                        // Then
                        result shouldBe false
                    }
                }
            }
        }
    })
