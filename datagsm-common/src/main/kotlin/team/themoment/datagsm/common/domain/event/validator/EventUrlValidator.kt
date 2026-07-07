package team.themoment.datagsm.common.domain.event.validator

import team.themoment.sdk.logging.logger.logger
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object EventUrlValidator {
    private const val DNS_TIMEOUT_SECONDS = 3L

    fun isPrivateAddress(address: InetAddress): Boolean =
        address.isLoopbackAddress ||
            address.isSiteLocalAddress ||
            address.isLinkLocalAddress ||
            address.isAnyLocalAddress

    fun isPrivateOrLocalUrl(url: String): Boolean =
        try {
            val host = URI(url).host ?: return true
            val addresses = resolveWithTimeout(host) ?: return true
            addresses.any { isPrivateAddress(it) }
        } catch (e: Exception) {
            logger().warn("Blocked event url due to unexpected parse error {}", e.message)
            true
        }

    // null = 차단 (타임아웃 또는 예상치 못한 오류)
    private fun resolveWithTimeout(host: String): Array<InetAddress>? =
        try {
            CompletableFuture
                .supplyAsync { InetAddress.getAllByName(host) }
                .get(DNS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            logger().warn("Blocked event url due to DNS lookup timeout for host {}", host)
            null
        } catch (e: Exception) {
            val cause = e.cause
            if (cause is UnknownHostException) {
                logger().warn("Blocked event url due to unknown host {}", host)
                null
            } else {
                logger().warn("Blocked event url due to DNS resolution failure for host {} error {}", host, e.message)
                null
            }
        }
}
