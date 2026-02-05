-- DataGSM 자퇴생 처리 방식 변경 마이그레이션
-- 작성일: 2026-02-05
-- 설명: isLeaveSchool Boolean 필드를 StudentRole.LEAVE_SCHOOL enum으로 전환

-- 1. 기존 isLeaveSchool=true 학생들을 LEAVE_SCHOOL role로 변경
UPDATE tb_student
SET role = 'LEAVE_SCHOOL'
WHERE is_leave_school = true;

-- 2. is_leave_school 컬럼 삭제
ALTER TABLE tb_student DROP COLUMN is_leave_school;
