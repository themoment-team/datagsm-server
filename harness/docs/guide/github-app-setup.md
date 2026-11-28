---
title: GitHub App 설정
description: AI Harness GitHub App 설치, 권한과 secret
order: 20
---

# GitHub App 설정

GitHub App을 대상 레포에 설치하면 하네스가 설치 범위 안의 `.harness/sync.yml`을 읽고 동기화 PR을 만들 수 있습니다. App을 새로 설치한 레포는 다음 동기화 실행부터 자동으로 대상에 포함됩니다.

## 권한

| 권한 | 용도 |
|------|------|
| Contents (Read & Write) | `.harness/sync.yml` 읽기, 동기화 브랜치에 파일 커밋 |
| Pull requests (Read & Write) | 동기화 PR 생성과 제목·대상 브랜치 수정 |
| Metadata (Read) | 설치된 레포 이름과 기본 브랜치 확인 |

## Secret

동기화 워크플로(`.github/workflows/datagsm-harness-sync.yaml`)는 datagsm-server 저장소의 Actions secret으로 App에 인증합니다.

| Secret | 값 |
|--------|----|
| `HARNESS_APP_ID` | GitHub App ID |
| `HARNESS_APP_PRIVATE_KEY` | App private key(`.pem`) 전체 내용 |
