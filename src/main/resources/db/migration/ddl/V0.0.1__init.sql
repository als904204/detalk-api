CREATE TABLE "member" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "login_type" VARCHAR(32) NOT NULL,
        "status" VARCHAR(32) NOT NULL,
        "created_at" BIGINT NOT NULL,
        "updated_at" BIGINT NOT NULL,
        "deleted_at" BIGINT,
        CONSTRAINT "member_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "attachment_file" (
        "id" UUID,
        "uploader_id" BIGINT NOT NULL,
        "name" VARCHAR(255) NOT NULL,
        "extension" VARCHAR(16),
        "url" VARCHAR NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "attachment_file_pkey" PRIMARY KEY ("id"),
        CONSTRAINT "uploader_id" FOREIGN KEY ("uploader_id") REFERENCES "member"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "member_profile" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "member_id" BIGINT NOT NULL,
        "avatar_id" UUID,
        "userhandle" VARCHAR(64) NOT NULL,
        "nickname" VARCHAR(32),
        "description" TEXT,
        "updated_at" BIGINT NOT NULL,
        CONSTRAINT "member_profile_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("avatar_id") REFERENCES "attachment_file" ("id") ON DELETE SET NULL ON UPDATE CASCADE,
        CONSTRAINT "member_profile_userhandle_key" UNIQUE ("userhandle")
);
CREATE INDEX "idx_member_profile_member_id" ON "member_profile" ("member_id");


-- 이메일은 확장성을 고려해 분리
-- 하나만 필요하면 하나의 row로 저장
CREATE TABLE "member_email" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "member_id" BIGINT NOT NULL,
        "value" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "member_email_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "member_email_member_id_value_key" UNIQUE ("member_id", "value")
);

CREATE TABLE "member_external" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "member_id" BIGINT NOT NULL,
        "type" VARCHAR(32) NOT NULL,
        "uid" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "member_external_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "member_external_member_id_type_value_key" UNIQUE ("member_id", "type", "uid")
);

CREATE TABLE "auth_refresh_token" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "member_id" BIGINT NOT NULL,
        "token" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        "expires_at" BIGINT NOT NULL,
        "revoked_at" BIGINT,
        CONSTRAINT "auth_refresh_token_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "auth_refresh_token_token_key" UNIQUE ("token")
);

CREATE TABLE "pricing_plan" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "name" VARCHAR(255) NOT NULL,
        CONSTRAINT "pricing_pkey" PRIMARY KEY ("id"),
        CONSTRAINT "pricing_plan_name_key" UNIQUE ("name")
);

CREATE TABLE "tag" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "name" VARCHAR(32) NOT NULL,
        CONSTRAINT "tag_pkey" PRIMARY KEY ("id"),
        CONSTRAINT "tag_name_key" UNIQUE ("name")
);

CREATE TABLE "product" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "name" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "product_pkey" PRIMARY KEY ("id"),
        CONSTRAINT "product_name_key" UNIQUE ("name")
);

CREATE TABLE "product_maker" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "product_id" BIGINT NOT NULL,
        "member_id" BIGINT NOT NULL,
        "created_at" BIGINT NOT NULL,
        "deleted_at" BIGINT,
        CONSTRAINT "product_maker_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "product_maker_product_id_member_id_key" UNIQUE ("product_id", "member_id")
);

CREATE TABLE "product_link" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "product_id" BIGINT NOT NULL,
        "url" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "product_link_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "idx_product_link_product_id" ON "product_link" ("product_id");

CREATE TABLE "product_post" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "writer_id" BIGINT NOT NULL,
        "product_id" BIGINT NOT NULL,
        "created_at" BIGINT NOT NULL,
        "recommend_count" BIGINT DEFAULT 0 NOT NULL,
        CONSTRAINT "product_post_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("writer_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "idx_product_post_id_created_at_desc" ON "product_post" ("id" DESC, "created_at" DESC);


CREATE TABLE "product_post_snapshot" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "post_id" BIGINT NOT NULL,
        "pricing_plan_id" BIGINT NOT NULL,
        "title" VARCHAR(255) NOT NULL,
        "description" TEXT NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "product_post_snapshot_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("post_id") REFERENCES "product_post" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("pricing_plan_id") REFERENCES "pricing_plan" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "product_post_last_snapshot" (
        "post_id" BIGINT NOT NULL,
        "snapshot_id" BIGINT NOT NULL,
        CONSTRAINT "product_post_last_snapshot_pkey" PRIMARY KEY ("post_id"),
        CONSTRAINT "product_post_last_snapshot_post_id_fkey" FOREIGN KEY ("post_id") REFERENCES "product_post"("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "product_post_last_snapshot_snapshot_id_fkey" FOREIGN KEY ("snapshot_id") REFERENCES "product_post_snapshot"("id") ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT "product_post_last_snapshot_snapshot_id_key" UNIQUE ("snapshot_id")
);

CREATE TABLE "product_post_snapshot_tag" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "post_id" BIGINT NOT NULL,
        "tag_id" BIGINT NOT NULL,
        CONSTRAINT "product_post_snapshot_tag_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("post_id") REFERENCES "product_post_snapshot" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("tag_id") REFERENCES "tag" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "idx_product_post_snapshot_tag_post_id_tag_id" ON "product_post_snapshot_tag" ("post_id", "tag_id");


CREATE TABLE "product_post_snapshot_attachment_file" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "snapshot_id" BIGINT NOT NULL,
        "attachment_file_id" UUID NOT NULL,
        "sequence" INT NOT NULL DEFAULT 0,
        CONSTRAINT "product_post_snapshot_attachment_file_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("snapshot_id") REFERENCES "product_post_snapshot" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("attachment_file_id") REFERENCES "attachment_file" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "idx_product_post_snapshot_attachment_file_snapshot_id" ON "product_post_snapshot_attachment_file" ("snapshot_id");
CREATE INDEX "idx_product_post_snapshot_attachment_file_attachment_file_id" ON "product_post_snapshot_attachment_file" ("attachment_file_id");

CREATE TABLE "recommend" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "value" VARCHAR(255) NOT NULL,
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "recommend_pkey" PRIMARY KEY ("id"),
        CONSTRAINT "recommend_value_key" UNIQUE ("value")
);

CREATE TABLE "recommend_product" (
        "id" BIGINT GENERATED ALWAYS AS IDENTITY,
        "recommend_id" BIGINT NOT NULL,
        "product_post_id" BIGINT NOT NULL,
        "member_id" BIGINT NOT NULL,
        "content" VARCHAR(255),
        "created_at" BIGINT NOT NULL,
        CONSTRAINT "recommend_product_pkey" PRIMARY KEY ("id"),
        FOREIGN KEY ("recommend_id") REFERENCES "recommend" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("product_post_id") REFERENCES "product_post" ("id") ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "role" (
    "code" VARCHAR(32) PRIMARY KEY,
    "description" VARCHAR(255)
);

CREATE TABLE "member_role" (
    "member_id" BIGINT NOT NULL,
    "role_code" VARCHAR(32) NOT NULL,
    PRIMARY KEY ("member_id", "role_code"),
    FOREIGN KEY ("member_id") REFERENCES "member" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("role_code") REFERENCES "role" ("code") ON DELETE CASCADE
);

-- 각 게시글이 어떤 링크를 사용했는지 연관관계 맺는 테이블
CREATE TABLE "product_post_link" (
    "post_id" BIGINT NOT NULL,
    "link_id" BIGINT NOT NULL,
    CONSTRAINT "product_post_link_pkey" PRIMARY KEY ("post_id", "link_id"),
    CONSTRAINT "product_post_link_post_fk" FOREIGN KEY ("post_id") REFERENCES "product_post"("id") ON DELETE CASCADE,
    CONSTRAINT "product_post_link_link_fk" FOREIGN KEY ("link_id") REFERENCES "product_link"("id") ON DELETE CASCADE
);

-- idempotent_key: 클라이언트가 생성한 UUID를 저장. UNIQUE 제약을 통해 중복 삽입 시 에러가 발생하게 함
CREATE TABLE product_post_idempotent_requests (
    "id" BIGINT GENERATED ALWAYS AS IDENTITY,
    "idempotent_key" UUID NOT NULL,
    "created_at" BIGINT NOT NULL,
    CONSTRAINT "ux_idempotent_key" UNIQUE ("idempotent_key")
);

-- 방문자 로그 테이블 생성: 사용자 위치 및 방문 정보를 저장
CREATE TABLE "visitor_log" (
    "id" BIGSERIAL PRIMARY KEY,
    "session_id" VARCHAR(255) NOT NULL, -- 방문자의 세션 ID
    "continent_code" CHAR(2) NOT NULL, -- 방문자의 대륙 코드 (예: 'NA', 'AS')
    "country_iso" CHAR(2) NOT NULL, -- 방문자의 국가 ISO 코드 (예: 'US', 'KR')
    "country_name" VARCHAR(64) NOT NULL, -- 방문자의 국가명 (예: 'United States', 'South Korea')
    "visited_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 방문 시각
    "user_agent" VARCHAR(512) NULL, -- 방문자의 브라우저 정보
    "referer" VARCHAR(512) NULL -- 이전 페이지 정보
);



