#!/usr/bin/env python3
"""한 sync 매트릭스 레그의 결과를 JSON 한 줄로 stdout에 출력한다.

각 스텝은 continue-on-error로 감싸 잡 자체는 항상 성공하므로, 잡 API의
conclusion은 실제 실패를 감춘다(masked). 그래서 각 레그가 스텝의 진짜
outcome을 읽어 상태를 직접 기록하고, report 잡이 이 파일을 취합한다.

env:
  REPO           대상 레포 full_name (owner/name)
  SERVER_URL     github.server_url
  JOB_NAME       이 레그의 잡 이름 (report가 로그를 찾을 때 매칭 키)
  BASE_BRANCH    sync 대상 base branch
  TOKEN_OUTCOME  토큰 생성 스텝 outcome
  BASE_OUTCOME   base branch 확인 스텝 outcome
  BASE_REASON    base branch 확인 사유 (ok|base-branch-missing|repo-empty)
  SYNC_OUTCOME   sync 액션 스텝 outcome
"""
import json
import os

token = os.environ.get("TOKEN_OUTCOME", "")
base = os.environ.get("BASE_OUTCOME", "")
base_reason = os.environ.get("BASE_REASON", "")
sync = os.environ.get("SYNC_OUTCOME", "")
base_branch = os.environ.get("BASE_BRANCH", "")

status = "success"
error_class = ""
message = ""

if token == "failure":
    status, error_class = "failed", "token-failed"
    message = "GitHub App 설치 토큰 생성에 실패했습니다 — 설치 권한을 확인하세요"
elif base == "failure" and base_reason == "repo-empty":
    status, error_class = "failed", "repo-empty"
    message = "원격 저장소가 비어 있어(커밋 없음) 동기화할 수 없습니다"
elif base == "failure":
    status, error_class = "failed", "base-branch-missing"
    message = f"`base_branch`로 지정된 `{base_branch}` 브랜치가 원격 저장소에 존재하지 않습니다"
elif sync == "failure":
    status, error_class = "failed", "sync-action-failed"
    message = ""  # 알 수 없는 오류 — report가 원시 로그를 첨부한다
elif sync == "success":
    status = "success"
else:
    # base 통과 후 sync가 실행되지 않은 예외적 상태
    status = "skipped"

record = {
    "repo": os.environ.get("REPO", ""),
    "repo_url": f'{os.environ.get("SERVER_URL", "https://github.com")}/{os.environ.get("REPO", "")}',
    "job_name": os.environ.get("JOB_NAME", ""),
    "status": status,
    "error_class": error_class,
    "message": message,
}
print(json.dumps(record))
