package team.themoment.datagsm.common.domain.webhook.service

import org.apache.hc.client5.http.DnsResolver
import org.apache.hc.client5.http.SystemDefaultDnsResolver
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.themoment.datagsm.common.domain.webhook.validator.WebhookUrlValidator
import team.themoment.sdk.logging.logger.logger
import java.net.InetAddress
import java.net.UnknownHostException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class WebhookSender {
    private val restClient: RestClient =
        RestClient
            .builder()
            .requestFactory(HttpComponentsClientHttpRequestFactory(buildSsrfSafeHttpClient()))
            .build()

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

    @Recover
    fun recover(
        e: Exception,
        targetUrl: String,
        secret: String,
        payloadJson: String,
    ) {
        logger().warn("Failed to deliver webhook after 3 attempts url {} error {}", targetUrl, e.message)
    }

    private fun buildSsrfSafeHttpClient(): CloseableHttpClient {
        val ssrfSafeDnsResolver =
            object : DnsResolver {
                override fun resolve(host: String): Array<InetAddress> {
                    val resolved: Array<InetAddress> =
                        try {
                            SystemDefaultDnsResolver.INSTANCE.resolve(host)
                        } catch (e: UnknownHostException) {
                            emptyArray()
                        }
                    if (resolved.any { WebhookUrlValidator.isPrivateAddress(it) }) {
                        throw UnknownHostException("Blocked private/local host $host")
                    }
                    return resolved
                }

                override fun resolveCanonicalHostname(host: String): String =
                    SystemDefaultDnsResolver.INSTANCE.resolveCanonicalHostname(host)
            }
        val connectionManager =
            PoolingHttpClientConnectionManagerBuilder
                .create()
                .setDnsResolver(ssrfSafeDnsResolver)
                .build()
        return HttpClients
            .custom()
            .setConnectionManager(connectionManager)
            .build()
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
