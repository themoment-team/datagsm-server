# Agents

Claude가 특정 트리거 문구 또는 상황을 감지하면 자동으로 위임하는 서브에이전트입니다.
직접 호출할 수도 있고, Claude가 컨텍스트를 보고 자동으로 실행하기도 합니다.

에이전트는 `sync-manifest.yml`의 `domain` 값(`frontend` / `backend` / `common`)을 기준으로 나눕니다.

## Frontend

| 에이전트 | 트리거 예시 | 설명 |
| --- | --- | --- |
| [frontend-convention-validator](./frontend-convention-validator.md) | `프론트엔드 컨벤션 검사해줘` | TypeScript/TSX 변경 파일을 도구 결과와 FSD 공식 구조·Query/Zod·Tailwind/shadcn 규칙 기준으로 읽기 전용 검사 |

## Backend

| 에이전트 | 트리거 예시 | 설명 |
| --- | --- | --- |
| [kotlin-convention-validator](./kotlin-convention-validator.md) | `컨벤션 검사해줘` | **Kotlin 전용.** git diff HEAD의 변경된 `.kt` 파일에서 컨벤션 위반 탐지 및 자동 수정 (DTO 어노테이션, 로깅, `val/var`, `@Transactional`) |
| [kotlin-test-fixer](./kotlin-test-fixer.md) | `테스트 고쳐줘`, `<모듈명> 테스트 고쳐줘` | **Kotlin 전용.** Kotest 테스트 실행 → 실패 진단 → 수정. 서비스 코드가 진실의 원천 — 서비스 버그는 서비스에서, 동작 변경은 테스트 업데이트. 최대 3회 재시도 |

## 공통

| 에이전트 | 트리거 예시 | 설명 |
| --- | --- | --- |
| [contradiction-finder](./contradiction-finder.md) | `모순 찾아줘`, `일관성 검사해줘` | 4계층 일관성 감사 — 문서↔문서, 문서↔코드, 문서↔에이전트/스킬, 에이전트↔에이전트 간 충돌 탐지. 파일 기반 리포트 출력 |
| [doc-polisher](./doc-polisher.md) | `문서 갱신해줘`, `AGENTS.md 업데이트해줘` | 실제 코드 패턴 기반으로 문서 코드 스니펫 갱신, 누락 컨벤션 추가, 구조 정리. 파일 직접 편집 (커밋 없음) |
| [prompt-polisher](./prompt-polisher.md) | `프롬프트 다듬어줘`, `에이전트 설명 다듬어줘` | 에이전트·스킬 파일의 영문 문법·프런트매터 완성도·트리거 문구 구체성 검사. Before/After diff 형식으로 제안만 (편집 없음) |
| [web-researcher](./web-researcher.md) | `최신 정보 조사해줘`, 릴리스 노트·CVE 질의 | 실시간 웹 정보 수집. 모델 학습 데이터 이후의 최신 사실·보안 권고·라이브러리 비교가 필요할 때 |
