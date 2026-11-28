# AI Harness

> Distribute AI tool configurations across all your repositories — automatically.

A central hub that shares Claude and Codex skills/agents/hooks and Gemini settings with every project that installs the GitHub App. Add a new project in seconds; no manual config editing required.

## How It Works

1. Push updates to this repo → GitHub Actions triggers
2. The workflow discovers every repository where the App is installed (across orgs and personal accounts)
3. A PR is opened in each target repo with the updated files

New projects are auto-detected the moment the App is installed — no `sync.yml` edits needed.

## Documentation

The Dashboard serves the public guide at `/guide`. The Markdown source is kept in this repository:

- [Getting Started](docs/guide/getting-started.md) — end-to-end setup for harness operators
- [GitHub App Setup](docs/guide/github-app-setup.md) — App creation, permissions, and secrets
- [Sync Configuration](docs/guide/sync-configuration.md) — control what each repo receives via `.harness/sync.yml`
- [Conventions](docs/guide/conventions/global.md) — commit, PR, and skill authoring conventions

Project instructions live in each repository’s `AGENTS.md` and are not synced. Skills and agents also work without that file by consulting existing convention documents and code. See [project instruction rules](docs/guide/conventions/global.md#프로젝트-지침-agentsmd).

## Directory Docs

- [Claude Skills](.claude/skills/README.md)
- [Claude Agents](.claude/agents/README.md)
- [Codex Skills](.agents/skills/README.md)

## License

MIT License
