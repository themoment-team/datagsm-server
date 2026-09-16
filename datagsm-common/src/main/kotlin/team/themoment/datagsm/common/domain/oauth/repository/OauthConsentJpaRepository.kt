package team.themoment.datagsm.common.domain.oauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.themoment.datagsm.common.domain.oauth.entity.OauthConsentJpaEntity
import java.util.Optional

interface OauthConsentJpaRepository : JpaRepository<OauthConsentJpaEntity, Long> {
    fun findByAccountIdAndClientId(
        accountId: Long,
        clientId: String,
    ): Optional<OauthConsentJpaEntity>

    /**
     * 동의 기록 행을 upsert하고 그 id를 확보한다.
     *
     * 같은 (account, client)로 첫 로그인이 동시에 들어오면 양쪽이 insert를 시도해
     * uk_oauth_consent_account_client 위반이 난다. 애플리케이션에서 조회 후 분기하면
     * 두 요청이 같은 순간 "없음"을 보고 둘 다 insert하는 창이 남으므로, DB가 원자적으로
     * 판단하도록 맡긴다.
     *
     * 중복 시 갱신할 값이 따로 없어 updated_at만 건드린다. 이 구문은 행이 반드시
     * 존재하도록 보장하는 것이 목적이고, id는 이어지는 findIdByAccountIdAndClientId로 읽는다.
     */
    @Modifying
    @Query(
        value =
            "INSERT INTO tb_oauth_consent (account_id, client_id, updated_at) " +
                "VALUES (:accountId, :clientId, NOW()) " +
                "ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at)",
        nativeQuery = true,
    )
    fun upsertConsent(
        @Param("accountId") accountId: Long,
        @Param("clientId") clientId: String,
    )

    /**
     * upsert 직후 행의 id를 읽는다.
     *
     * 네이티브 쿼리라 영속성 컨텍스트의 1차 캐시나 REPEATABLE READ 스냅샷이 아니라
     * upsert가 방금 확정한 행을 그대로 본다.
     */
    @Query(
        value = "SELECT id FROM tb_oauth_consent WHERE account_id = :accountId AND client_id = :clientId",
        nativeQuery = true,
    )
    fun findIdByAccountIdAndClientId(
        @Param("accountId") accountId: Long,
        @Param("clientId") clientId: String,
    ): Long?

    /**
     * scope를 추가한다. 이미 있는 scope는 무시한다.
     *
     * tb_oauth_consent_scope에는 유니크 제약이 없어 INSERT IGNORE로는 중복이 쌓이므로,
     * 존재하지 않을 때만 넣도록 NOT EXISTS로 거른다.
     */
    @Modifying
    @Query(
        value =
            "INSERT INTO tb_oauth_consent_scope (consent_id, scope) " +
                "SELECT :consentId, :scope FROM DUAL WHERE NOT EXISTS (" +
                "SELECT 1 FROM tb_oauth_consent_scope WHERE consent_id = :consentId AND scope = :scope)",
        nativeQuery = true,
    )
    fun addScopeIfAbsent(
        @Param("consentId") consentId: Long,
        @Param("scope") scope: String,
    )
}
