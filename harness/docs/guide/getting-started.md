---
title: AI Harness 가이드
description: 하네스 설치와 문서 탐색
order: 10
---

# AI Harness 가이드

GitHub App이 설치된 레포는 Claude, Codex, Gemini 설정을 동기화받습니다. `.harness/sync.yml`로 받을 항목을 선택할 수 있습니다.

## 빠른 시작

```yaml
# .harness/sync.yml
groups:
  - claude
  - codex
```

GitHub App 설치 후 이 설정을 기본 브랜치에 두면 Claude Code 스킬·에이전트와 Codex 스킬·에이전트·설정이 동기화 PR로 전달됩니다. PR을 병합하면 파일이 적용됩니다.

세부 제어가 필요하다면 [동기화 설정](./sync-configuration.md)를 참고하세요.

프로젝트 지침인 `AGENTS.md`는 각 레포가 직접 관리하며 동기화 대상이 아닙니다. 파일이 없는 경우의 처리 기준은 [전역 컨벤션](./conventions/global.md#프로젝트-지침-agentsmd)을 참고하세요.

## 문서

### 설정
- [GitHub App 설정](./github-app-setup.md) — App 설치, 로그인과 레포 접근 권한
- [대시보드 사용법](./dashboard-guide.md) — 설정 PR 생성과 즉시 동기화
- [동기화 설정](./sync-configuration.md) — 그룹 선택, 훅 활성화, 버전 고정 등 동기화 항목 제어

### 레퍼런스
- [스킬 레퍼런스](./reference/skills.md) — 스킬 목록과 각 스킬의 역할
- [에이전트 레퍼런스](./reference/agents.md) — 서브에이전트 목록과 트리거 문구
- [훅 레퍼런스](./reference/hooks.md) — 훅 모듈 목록과 프로젝트 유형별 추천 조합
- [프론트엔드 아키텍처](./architecture/frontend.md) — Next.js App Router, FSD, Turborepo 프론트엔드 규칙

### 컨벤션
- [Claude 컨벤션](./conventions/claude.md) — 스킬·에이전트·훅 작성 규칙
- [Codex 컨벤션](./conventions/codex.md) — Codex 설정·훅 작성 규칙
- [Gemini 컨벤션](./conventions/gemini.md) — Gemini 설정·스타일가이드 작성 규칙
- [Copilot 컨벤션](./conventions/copilot.md) — copilot-instructions.md 작성 규칙
- [전역 컨벤션](./conventions/global.md) — 커밋·PR·브랜치 공통 규칙
