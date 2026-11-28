---
title: GitHub App 설정
description: AI Harness GitHub App 설치와 대시보드 로그인 설정
order: 20
---

# GitHub App 설정

GitHub App을 대상 레포에 설치하면 Harness가 설치 범위 안의 파일을 읽고 동기화 PR을 만들 수 있습니다.

대시보드 로그인은 GitHub App의 사용자 인증을 사용합니다. Callback URL에는 배포 주소의 다음 경로를 등록합니다.

```text
https://dashboard.example.com/api/auth/callback/github
```

로컬 개발 주소는 `http://localhost:3000/api/auth/callback/github`입니다.

프로젝트를 대시보드에서 설정하려면 해당 레포의 `write` 이상 권한이 필요합니다.
