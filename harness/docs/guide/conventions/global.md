---
title: 전역 컨벤션
description: 커밋, PR, 브랜치 공통 규칙
order: 50
---

# Global Conventions

모든 스코프에 공통으로 적용되는 규칙입니다.

## 프로젝트 지침 (`AGENTS.md`)

프로젝트 공통 지침은 각 저장소의 `AGENTS.md`에서 관리합니다. 하위 디렉터리의 파일은 해당 경로에만 적용하며, 같은 범위에서 충돌하면 더 가까운 지침을 따릅니다. 하네스는 이 파일을 배포하거나 덮어쓰지 않습니다.

하네스의 규칙 검사·문서 정리·리뷰 대응 도구는 런타임이 적용한 지침과 프로젝트가 명시한 우선순위를 따릅니다. `.claude/rules/**`, `.gemini/styleguide.md`, `CONTRIBUTING.md`, `.github/copilot-instructions.md`는 작업과 관련된 기존 내용만 참고하며, 도구 이름으로 고정 순위를 매기지 않습니다. 우선순위가 명시되지 않은 충돌은 양쪽 근거를 보고하고 해당 변경만 보류합니다.

`AGENTS.md`가 없어도 작업을 계속합니다. 없는 문서는 건너뛰며, 규칙이 문서화되어 있지 않으면 코드·도구 설정을 근거로 삼되 추론한 관례임을 밝힙니다. 없는 파일이나 섹션을 인용하거나 지침 파일을 자동 생성하지 않습니다. 문서나 도구 설정의 근거가 없는 스타일 변경은 자동 수정 대신 제안으로 남깁니다.

자동 로딩과 수동 참조는 구분합니다. Codex는 같은 디렉터리의 `AGENTS.override.md`를 `AGENTS.md`보다 먼저 선택하며, 설정된 대체 파일명도 사용할 수 있습니다. 이렇게 선택되지 않은 문서를 감사 대상으로 읽더라도 활성 지침으로 다시 적용하지 않습니다. 이 선택 규칙을 다른 도구에도 동일하게 적용한다고 가정하지 마세요. 자세한 내용은 [Codex 공식 지침 문서](https://developers.openai.com/codex/guides/agents-md)를 참고하세요.

### 문서 유지 원칙

루트 `AGENTS.md`에는 공통 제약과 상세 문서 위치를 간결하게 둡니다. 이 저장소의 운영 문서는 `docs/guide/`에서 관리하며, Wiki 대응 문서도 같은 변경에서 점검합니다. 개별 설치되는 스킬·에이전트에는 실행에 필요한 지침을 남겨 타깃 레포에 하네스 원본 문서가 없어도 사용할 수 있게 합니다.

이 구조는 상세 지식을 저장소 문서로 분리하는 [OpenAI의 하네스 설계 사례](https://openai.com/index/harness-engineering/)와 필요한 맥락을 작업 중에 읽는 [Anthropic의 컨텍스트 설계 원칙](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents)을 참고한 선택입니다. 실제 성능 향상을 입증하는 벤치마크 결과를 뜻하지는 않습니다.

## 커밋 컨벤션

```
type(scope): 한국어 설명
```

### type

| type | 사용 시점 |
|------|----------|
| `add` | 새 스킬·에이전트·설정 추가 |
| `update` | 기존 항목 개선·수정 |
| `fix` | 오류 수정 |
| `docs` | README·Wiki 등 문서 변경 |
| `ci/cd` | 워크플로우·스크립트 변경 |
| `refactor` | 구조 개선 (기능 변경 없음) |

### scope

| scope | 대상 |
|-------|------|
| `global` | 여러 스코프에 걸친 변경, 프로젝트 메타 파일 |
| `claude` | `.claude/` 하위 파일 |
| `codex` | `.codex/`, `.agents/` 하위 파일 |
| `gemini` | `.gemini/` 하위 파일 |
| `copilot` | `.github/copilot-instructions.md` |

### 설명 규칙

- 한국어, 마침표 없음
- 명사형으로 끝내기 (`추가`, `수정`, `개선`)
- 50자 이내

**예시**

```
add(claude): systematic-debugging 스킬 추가
update(global): write-pr 스킬 레퍼런스 경로 수정
docs(global): README 동기화 방식 설명 업데이트
ci/cd(global): 파일 동기화 워크플로우 추가
```

## PR 컨벤션

- 제목: 커밋 컨벤션과 동일한 형식
- 라벨: `harness-sync` (자동 동기화 PR), `enhancement`, `bug`, `documentation`
- 자동 동기화 PR은 리뷰 후 머지 — 무조건 Auto-merge 금지

## 브랜치 컨벤션

```
type/scope-description
```

**예시**

```
add/claude-new-skill
update/global-sync-workflow
fix/codex-hook-path
```

## 파일 네이밍

- 디렉토리: `kebab-case`
- 파일: `kebab-case.md`, `kebab-case.sh`
- 스킬 진입점: 반드시 `SKILL.md` (대문자)
- 에이전트: Claude는 `kebab-case.md`, Codex는 `kebab-case.toml`
