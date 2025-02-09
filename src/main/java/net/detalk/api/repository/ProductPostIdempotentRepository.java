package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPostIdempotentRequests.PRODUCT_POST_IDEMPOTENT_REQUESTS;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductPostIdempotentRepository {

    private final DSLContext dsl;

    /**
     * 새로 들어온 멱등 키를 DB에 삽입 시도
     * 성공 시 @return true (1)
     * 실패 시 @return false (0)
     */
    public boolean insertIdempotentKey(UUID idempotentKey, Instant now) {
        int rows = dsl
            .insertInto(PRODUCT_POST_IDEMPOTENT_REQUESTS)
            .set(PRODUCT_POST_IDEMPOTENT_REQUESTS.IDEMPOTENT_KEY, idempotentKey)
            .set(PRODUCT_POST_IDEMPOTENT_REQUESTS.CREATED_AT, now)
            // PostgreSQL onConflict (중복 충돌이면, 아무것도 하지 않음)
            .onConflictDoNothing()
            .execute();
        if (rows != 1) {
            log.warn("게시글 업로드 충돌발생 key={}", idempotentKey);
            return false;
        }
        return true;
    }
}
