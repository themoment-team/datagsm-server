package team.themoment.datagsm.common.domain.event.service

import org.apache.hc.client5.http.DnsResolver
import org.apache.hc.client5.http.SystemDefaultDnsResolver
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.util.Timeout
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.themoment.datagsm.common.domain.event.validator.EventUrlValidator
import java.net.InetAddress
import java.net.UnknownHostException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class EventSender {
    private val restClient: RestClient =
        RestClient
            .builder()
            .requestFactory(HttpComponentsClientHttpRequestFactory(buildSsrfSafeHttpClient()))
            .build()

    companion object {
        private val CONNECT_TIMEOUT = Timeout.ofSeconds(3)
        private val RESPONSE_TIMEOUT = Timeout.ofSeconds(5)
        private val CONNECTION_LEASE_TIMEOUT = Timeout.ofSeconds(3)
    }

    // @Recover를 두지 않아 재시도 소진 시 마지막 예외가 그대로 전파된다.
    // 실패 원인 분류와 알림은 대상별 격리를 담당하는 EventPublisher가 처리한다.
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 1000, multiplier = 10.0))
    fun send(
        targetUrl: String,
        secret: String,
        payloadJson: String,
    ) {
        val signature = computeHmacSha256(secret, payloadJson)
        restClient
            .post()
            .uri(targetUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-DataGSM-Signature", "sha256=$signature")
            .body(payloadJson)
            .retrieve()
            .toBodilessEntity()
    }

    private fun buildSsrfSafeHttpClient(): CloseableHttpClient {
        val ssrfSafeDnsResolver =
            object : DnsResolver {
                override fun resolve(host: String): Array<InetAddress> {
                    val resolved: Array<InetAddress> =
                        try {
                            SystemDefaultDnsResolver.INSTANCE.resolve(host)
                        } catch (_: UnknownHostException) {
                            emptyArray()
                        }
                    if (resolved.any { EventUrlValidator.isPrivateAddress(it) }) {
                        throw UnknownHostException("Blocked private/local host $host")
                    }
                    return resolved
                }

                override fun resolveCanonicalHostname(host: String): String =
                    SystemDefaultDnsResolver.INSTANCE.resolveCanonicalHostname(host)
            }
        // 타임아웃이 없으면 응답하지 않는 소비자 하나가 @Async 스레드를 무기한 점유한다.
        val connectionManager =
            PoolingHttpClientConnectionManagerBuilder
                .create()
                .setDnsResolver(ssrfSafeDnsResolver)
                .setDefaultConnectionConfig(
                    ConnectionConfig
                        .custom()
                        .setConnectTimeout(CONNECT_TIMEOUT)
                        .setSocketTimeout(RESPONSE_TIMEOUT)
                        .build(),
                ).build()
        return HttpClients
            .custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(
                RequestConfig
                    .custom()
                    .setConnectionRequestTimeout(CONNECTION_LEASE_TIMEOUT)
                    .setResponseTimeout(RESPONSE_TIMEOUT)
                    .build(),
            ).build()
    }

    private fun computeHmacSha256(
        secret: String,
        payload: String,
    ): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac
            .doFinal(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
