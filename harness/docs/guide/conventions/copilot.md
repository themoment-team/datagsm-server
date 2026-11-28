---
title: Copilot 지침
description: Copilot 지침 작성 규칙
order: 90
---

# Copilot Conventions

`.github/copilot-instructions.md` 작성 규칙입니다.

> **참고**: `copilot-instructions.md`는 harness 동기화 대상이 아닙니다. 각 레포가 직접 작성·관리하세요. 아래는 작성 가이드입니다.  
> ai-harness의 `.github/copilot-instructions.md`는 이 레포 자체를 위한 파일입니다.

프로젝트 공통 규칙은 `AGENTS.md`에 정리하고, 이 파일에는 Copilot에 필요한 안내를 유지합니다. 같은 규칙을 두 곳에 관리한다면 내용이 충돌하지 않도록 확인하세요. `AGENTS.md`가 없는 경우에도 기존 지침 파일을 사용할 수 있습니다.

## 파일 위치

```
project-root/
└── .github/
    └── copilot-instructions.md
```

## 기본 구조

```markdown
# [프로젝트명] Copilot Instructions

**한국어로 응답해주세요.**

## Project Overview
[프로젝트 1-2줄 요약]

## Tech Stack
[핵심 기술 스택 목록]

## Project Structure
[디렉토리 구조 또는 모듈 설명]

## Commands
[빌드·테스트·실행 명령어]

## Coding Conventions
[핵심 컨벤션 — 상세 내용은 하위 섹션으로]

## Common Mistakes (Avoid These!)
[자주 틀리는 패턴 — Before/After 형식]
```

## 작성 원칙

- **응답 언어 선언** 최상단 필수: `**한국어로 응답해주세요.**`
- **Project Overview**는 2문장 이내
- **Coding Conventions**는 핵심만 — 너무 길면 Copilot이 무시
- **Common Mistakes** 섹션이 가장 효과적 — WRONG/CORRECT 형식 사용
- Markdown 코드 블록에 언어 명시 (`kotlin`, `bash` 등)
- API 키·시크릿 절대 포함 금지

## Common Mistakes 작성 예시

```markdown
## Common Mistakes (Avoid These!)

### DTO Annotations
- WRONG: `@param:JsonProperty` → CORRECT: `@field:JsonProperty`

### Commit Scope
- WRONG: `fix(web):` (module name) → CORRECT: `fix(auth):` (domain name)
```

## 업데이트 주기

프로젝트 컨벤션 변경 시 즉시 반영합니다.  
`doc-polisher` 에이전트로 실제 코드와의 괴리를 주기적으로 점검하세요.
