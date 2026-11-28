# Privacy Policy

**Effective date:** 2025-05-26

AI Harness ("the App") is a GitHub App that distributes AI tool configuration files across repositories using GitHub Actions. This policy describes what data the App accesses and how it is handled.

## Data Accessed

The App requests the following GitHub permissions:

| Permission | Purpose |
|------------|---------|
| **Contents** (Read & Write) | Read `.harness/sync.yml` from target repositories to determine sync preferences; write synced configuration files via pull requests |
| **Pull requests** (Read & Write) | Open pull requests in target repositories with updated configuration files |
| **Metadata** (Read) | Read repository names and ownership to identify sync targets |

## Data Storage

The App does **not** operate any external servers or databases. All processing occurs entirely within GitHub Actions runners, which are ephemeral — no data persists after a workflow run completes.

## Data Sharing

No data is transmitted to any third party. The App communicates only with the GitHub API using installation tokens scoped to the repositories it is installed on.

## Personal Data

The App does not collect, process, or store personal data beyond the repository metadata (repository name and owner login) provided by the GitHub API during installation discovery. This information is used solely to route file sync operations and is not retained after the workflow run.

## Security

Authentication uses short-lived GitHub App installation tokens (valid for 1 hour) generated at runtime. No long-lived credentials are stored in the workflow beyond the App private key, which is held as an encrypted GitHub Actions secret.

## Contact

For questions or concerns about this policy, open an issue at:  
https://github.com/themoment-team/themoment-ai-harness-sync/issues