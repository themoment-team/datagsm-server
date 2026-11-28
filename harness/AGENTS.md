**한국어로 응답하고 작업해주세요 (Please respond and work in Korean).**

## Overview

This directory is the AI harness source inside the datagsm-server repository. It distributes AI tool configurations — Claude and Codex skills/agents/hooks and Gemini settings — to multiple projects via GitHub App and automated sync.

`harness/` 아래 변경이 `master`에 반영되면 `.github/workflows/datagsm-harness-sync.yaml`이 GitHub App이 설치된 타깃 레포로 동기화 PR을 생성합니다. datagsm-server도 설치 레포라면 같은 방식으로 동기화 PR을 받습니다. 각 타깃 레포는 `.harness/sync.yml`로 동기화 항목을 선택합니다.

manifest의 `src`와 이 문서의 경로는 `harness/` 기준입니다. 단, `.github/workflows/`는 저장소 루트에 있습니다. 동기화 워크플로가 `src` 앞에 `harness/`를 붙이고, `dest`는 타깃 레포 루트 기준으로 그대로 씁니다.

## 프로젝트 지침

- 공통 지침은 `AGENTS.md`에서 관리합니다. 하위 디렉터리의 `AGENTS.md`는 해당 경로에만 적용하며, 같은 범위에서 충돌하면 더 가까운 파일의 지침을 따릅니다.
- 배포되는 스킬과 에이전트는 타깃 레포에 `AGENTS.md`가 있다고 가정하지 않습니다. 파일이 없으면 적용 가능한 `.claude/rules/**`, `.gemini/styleguide.md`, `CONTRIBUTING.md`, `.github/copilot-instructions.md` 중 작업에 관련된 기존 문서를 참고합니다.
- 런타임이 적용한 지침과 프로젝트가 명시한 우선순위를 따릅니다. 보조 문서 간 우선순위를 임의로 정하지 않으며, 해결되지 않는 충돌은 근거와 함께 보고하고 해당 변경만 보류합니다. Codex의 `AGENTS.override.md` 등 런타임별 선택 규칙도 존중합니다.
- 규칙 문서가 없거나 특정 주제를 다루지 않으면 기존 코드·도구 설정을 참고하고, 추론한 관례와 명시된 규칙을 구분합니다. 없는 파일이나 섹션을 근거로 인용하거나 지침 파일을 임의로 생성하지 않습니다.
- 프로젝트 지침은 각 타깃 레포가 직접 관리합니다. 이 레포의 `AGENTS.md`를 동기화 항목이나 타깃 레포 삭제 목록에 등록하지 않습니다.

## Commit Conventions

커밋 메시지와 PR 제목은 저장소 루트 `AGENTS.md`의 규칙을 따릅니다. 하네스 원본(`harness/`)만 바꾸는 변경은 `global` 스코프를 씁니다. 워크플로 변경은 `ci/cd(ci/cd)`를 씁니다.

## Adding a New Skill

1. Add skill files under `.claude/skills/<name>/` (and mirror in `.agents/skills/<name>/` for Codex)
2. Register each new path in `sync-manifest.yml` under `items:` with a unique `id` and a `domain` (`frontend` / `backend` / `common`). `domain`은 동기화 항목 화면과 README 분류에만 쓰이며 동기화 대상 선택에는 영향을 주지 않습니다.
3. Commit: `add(global): <이름> 스킬 추가`

## Adding a New Agent

Claude와 Codex는 에이전트 포맷이 다릅니다. 본문은 같아도 헤더 형식을 각각 맞춰야 합니다.

1. Claude: `.claude/agents/<name>.md` 작성 — YAML frontmatter (`name`, `description` 필수; `tools`, `model`, `color`, `memory`, `maxTurns`, `permissionMode` 선택) + 본문 프롬프트
   Codex: `.codex/agents/<name>.toml` 작성 — `name`, `description`, `developer_instructions` 필수; 선택 필드 `model_reasoning_effort`(`low`/`medium`/`high`), `sandbox_mode`(`read-only`/`workspace-write`), `mcp_servers`, `skills.config`, `nickname_candidates`
2. 포맷 변환 주의:
   - `developer_instructions`는 TOML **literal string**(`'''…'''`)으로 작성한다 — 본문의 grep 정규식 백슬래시가 basic string 이스케이프와 충돌함
   - Claude `tools:` allowlist는 Codex에 1:1 대응이 없다 → 읽기 전용 에이전트는 `sandbox_mode = "read-only"`, 편집 에이전트는 `"workspace-write"`로 매핑
   - Codex `model`은 잘못된 ID 위험이 있으므로 생략해 부모 세션(`.codex/config.toml`) 설정을 상속하고, 작업 무게는 `model_reasoning_effort`로 차등한다
3. `sync-manifest.yml`에 양쪽 경로를 각각 등록 (`claude/agents/<name>`, `codex/agents/<name>`, `groups: [claude]` / `[codex]`, `domain: frontend|backend|common`)
4. `.claude/agents/README.md`와 `.codex/agents/README.md` 표에 항목 추가
5. Commit: `add(global): <이름> 에이전트 추가`

## Adding a New Hook Module

Hook 모듈은 dispatcher가 자동으로 스캔하는 구조입니다.

1. Claude: `.claude/hooks/modules/<name>/preToolUse.sh`, `postToolUse.sh`, `sessionStart.sh` 중 필요한 파일 작성
   Codex: `.codex/hooks/modules/<name>/pre-tool-use.sh`, `post-tool-use.sh`, `session-start.sh` 중 필요한 파일 작성
2. 모듈 인터페이스: `exit 2` = 실행 차단, `exit 0` = 정상 통과. 세션 시작 모듈은 stdout으로 출력한 텍스트가 세션 컨텍스트에 추가된다
3. `sync-manifest.yml`에 항목 등록 (`groups: []` = opt-in 전용)
4. Commit: `add(global): <이름> hook 모듈 추가`

## 동기화 설정과 문서

- 타깃 레포는 `.harness/sync.yml`의 `groups`와 `overrides`로 배포 항목을 선택합니다. 구형 `exclude`/`include`도 하위 호환으로 지원합니다.
- `enabled`, `language`, `pr_label`, 버전 고정과 예시는 [동기화 설정](docs/guide/sync-configuration.md)을 참고하세요.
- 훅 구조와 모듈 인터페이스는 [Claude 컨벤션](docs/guide/conventions/claude.md)과 [Codex 컨벤션](docs/guide/conventions/codex.md)을 참고하세요.
- 운영 문서는 `docs/guide/`에서 관리합니다. 운영 정책을 바꾸면 이 문서도 같은 변경에서 고칩니다.
- 배포된 스킬·에이전트는 독립적으로 설치될 수 있으므로 필요한 실행 규칙을 자체적으로 포함합니다. 이 저장소의 루트 지침이 타깃 레포에도 있다고 가정하지 않습니다.

## 파일 이름 변경 / 삭제 시 타깃 레포 정리

배포 항목을 삭제하거나 이름을 바꾸면 `sync-manifest.yml`의 `deletions`에 이전 배포 경로를 등록합니다. `dest`는 필수이며, `reason`과 `since`는 선택입니다. 모든 타깃 레포의 정리가 완료된 후 목록에서 제거합니다. 프로젝트가 직접 관리하는 지침 파일은 이 목록에 추가하지 않습니다.

## Breaking Change 릴리스 (버전 아카이브)

스킬/에이전트에 breaking change를 적용하기 전:

1. 기존 파일을 `_archive/<name>/<vN>/` 에 복사
   ```
   cp -r .claude/skills/git-commit .claude/skills/_archive/git-commit/v1
   cp -r .agents/skills/git-commit .agents/skills/_archive/git-commit/v1
   ```
2. `sync-manifest.yml`에 `@v1` 항목 등록 (`groups: []`)
3. 최신 스킬을 수정한다
4. `docs/guide/sync-configuration.md`에 변경 내용 기재
5. Commit: `update(global): git-commit v2, archive v1`

타깃 레포는 `overrides: { claude/skills/git-commit: "v1" }` 로 구 버전을 유지할 수 있다.

## 변경 검증

스킬·에이전트·훅·manifest를 바꿨으면 커밋 전에 `harness/`에서 실행합니다. PR에서는 `.github/workflows/datagsm-harness-validate.yaml`이 같은 검사를 돌립니다. Python 3.11 이상(`tomllib`)과 PyYAML이 필요합니다.

```
cd harness
pip install pyyaml
python3 scripts/validate-manifest.py --base origin/develop
python3 -m unittest discover -s scripts -p 'test_*.py'
```

- 에러: manifest 형식, 스킬·에이전트 `domain` 누락, 없는 `src`, 미등록 스킬·에이전트·훅 모듈, frontmatter·TOML·JSON 문법, Codex 에이전트 필드 규칙
- 경고: Claude/Codex 한쪽에만 있는 항목, `--base` 이후 한쪽만 수정된 항목. Claude 전용 항목처럼 의도한 경우는 그대로 둡니다

## Key Files

- `sync-manifest.yml` — available sync groups and item registry
- `scripts/list-installed-repos.py` — discovers App installations and generates per-repo sync configs
- `scripts/validate-manifest.py` — manifest와 배포 파일 검사 (PR 검증과 로컬 공용)
- `.github/workflows/datagsm-harness-sync.yaml` (저장소 루트) — auto-sync workflow (matrix per repo)
- `.github/workflows/datagsm-harness-validate.yaml` (저장소 루트) — PR 검증 워크플로 (manifest·스크립트 테스트)
- `.harness/sync.yml.example` — template for target repos to opt-in/opt-out of specific items
- `.claude/templates/settings-base.json` — hooks 미포함 시 전파되는 settings.json 기반 파일
- `docs/guide/` — 하네스 운영·사용 문서
