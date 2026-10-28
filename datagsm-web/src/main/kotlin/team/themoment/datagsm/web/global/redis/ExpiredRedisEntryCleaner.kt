package team.themoment.datagsm.web.global.redis

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import team.themoment.sdk.logging.logger.logger

/**
 * keyspace events를 쓰지 않으므로 TTL로 만료된 엔티티의 id가 키스페이스 세트와 인덱스 세트에 남는다.
 * 본 데이터가 사라진 id만 원자적으로 걷어내므로, 살아 있는 엔티티와 그 인덱스는 건드리지 않는다.
 */
@Component
class ExpiredRedisEntryCleaner(
    private val redisKeyValueAdapter: RedisKeyValueAdapter,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val cleanupScript = DefaultRedisScript(CLEANUP_SCRIPT, Long::class.java)

    // 대상 엔티티는 배포 단위로 고정이므로 한 번만 찾는다. 실패하면 다음 실행에서 다시 찾는다.
    private val expiringKeyspaces: List<String> by lazy { findExpiringKeyspaces() }

    fun cleanUp(): Map<String, Long> {
        val keyspaces =
            runCatching { expiringKeyspaces }
                .onFailure { logger().error("Failed to find expiring Redis keyspaces", it) }
                .getOrElse { return emptyMap() }
        return keyspaces.associateWith { cleanUpKeyspace(it) }
    }

    internal fun findExpiringKeyspaces(): List<String> {
        val mappingContext = redisKeyValueAdapter.converter.mappingContext
        val classLoader = javaClass.classLoader
        return ClassPathScanningCandidateComponentProvider(false)
            .apply { addIncludeFilter(AnnotationTypeFilter(RedisHash::class.java)) }
            .findCandidateComponents(ENTITY_BASE_PACKAGE)
            .mapNotNull { it.beanClassName }
            .map { mappingContext.getRequiredPersistentEntity(ClassUtils.forName(it, classLoader)) }
            .filter { it.isExpiring }
            .mapNotNull { it.keySpace }
            .distinct()
            .sorted()
    }

    // 키스페이스 하나가 실패해도 나머지는 계속 처리하고, 이미 지운 개수는 실패해도 그대로 보고한다.
    private fun cleanUpKeyspace(keyspace: String): Long {
        var removedCount = 0L
        try {
            redisTemplate
                .opsForSet()
                .scan(keyspace, ScanOptions.scanOptions().count(SCAN_COUNT).build())
                .use { cursor ->
                    cursor
                        .asSequence()
                        .chunked(BATCH_SIZE)
                        .forEach { ids -> removedCount += removeExpired(keyspace, ids) }
                }
        } catch (e: Exception) {
            logger().error("Failed to clean up expired entries in keyspace {} after removing {}", keyspace, removedCount, e)
        }
        return removedCount
    }

    private fun removeExpired(
        keyspace: String,
        ids: List<String>,
    ): Long {
        // 본 데이터 키는 리포지토리가 조회할 때와 같은 규칙으로 만들어야 살아 있는 엔티티를 만료로 오인하지 않는다.
        val keys =
            listOf(keyspace) +
                ids.flatMap { id ->
                    listOf(String(redisKeyValueAdapter.createKey(keyspace, id), Charsets.UTF_8), "$keyspace:$id:idx")
                }
        return redisTemplate.execute(cleanupScript, keys, *ids.toTypedArray()) ?: 0L
    }

    private companion object {
        const val ENTITY_BASE_PACKAGE = "team.themoment.datagsm.common.domain"
        const val SCAN_COUNT = 500L
        const val BATCH_SIZE = 200

        // KEYS[1]은 키스페이스 세트, 이후 id마다 본 데이터 키와 인덱스 보조 키가 이어진다.
        const val CLEANUP_SCRIPT = """
local removed = 0
for i, id in ipairs(ARGV) do
  local entryKey = KEYS[i * 2]
  local indexHelperKey = KEYS[i * 2 + 1]
  if redis.call('EXISTS', entryKey) == 0 then
    for _, indexKey in ipairs(redis.call('SMEMBERS', indexHelperKey)) do
      if redis.call('TYPE', indexKey).ok == 'zset' then
        redis.call('ZREM', indexKey, id)
      else
        redis.call('SREM', indexKey, id)
      end
    end
    redis.call('DEL', indexHelperKey)
    redis.call('SREM', KEYS[1], id)
    removed = removed + 1
  end
end
return removed
"""
    }
}
