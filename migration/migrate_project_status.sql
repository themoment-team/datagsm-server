-- Issue #294: 프로젝트 운영 상태 관리 추가
-- tb_project 테이블에 start_year, end_year, status 컬럼 추가

ALTER TABLE tb_project
    ADD COLUMN start_year INT         NOT NULL DEFAULT 0,
    ADD COLUMN end_year   INT         NULL,
    ADD COLUMN status     VARCHAR(10) NOT NULL DEFAULT 'ACTIVE';
