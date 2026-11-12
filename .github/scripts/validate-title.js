// Shared by the CI (PR title) and issue validation workflows via actions/github-script.
const ALLOWED_SCOPES = [
  'global', 'account', 'auth', 'client', 'club', 'neis', 'oauth', 'project',
  'student', 'utility', 'web', 'openapi', 'ci/cd', 'application', 'event',
];
const TITLE_PATTERN = /^\[([^\]]+)\]\s+(.+)$/;
const VERSION_PATTERN = /^v\d{8}\.\d+$/;

module.exports = ({ core, title, allowVersion = false }) => {
  if (allowVersion && VERSION_PATTERN.test(title)) {
    return;
  }

  const match = title.match(TITLE_PATTERN);
  if (!match) {
    core.setFailed(allowVersion
      ? 'Invalid title format. Use [scope] title or vYYYYMMDD.n format'
      : 'Invalid title format. Use [scope] title');
    return;
  }

  const [, scopesPart, titlePart] = match;
  if (titlePart.includes(':')) {
    core.setFailed('Colon (:) is not allowed in the title');
    return;
  }

  const invalidScopes = scopesPart
    .split(',')
    .map((scope) => scope.trim())
    .filter((scope) => !ALLOWED_SCOPES.includes(scope));
  if (invalidScopes.length > 0) {
    core.setFailed(`Invalid scope(s): ${invalidScopes.join(', ')}`);
  }
};
