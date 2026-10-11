# 0002. Define requirements as GitHub issues before planning (2026-10-11)

## Context

Once an idea is chosen, the next step is planning and then implementation. Without written acceptance criteria, the plan has nothing to be checked against, and reviews drift into "does this feel right?". The requirements also need to stay discussable while they change. The process should stay light for a solo side project.

## Options

1. Requirements as GitHub issues, one per chosen idea, with acceptance criteria.
2. Requirements as versioned docs under `docs/requirements/`, reviewed through PRs.
3. No written requirements; go straight from the idea doc to the plan.

## Decision and why

Option 1. Issues are easy to discuss and comment on while the requirements change, and a PR can close one. The issue holds the *what*. This record holds the *why*, and the idea doc links to both. Option 2 is versioned, but every wording change becomes a PR, which is heavy at this stage. Option 3 leaves nothing to check the plan against.

Each issue stays about one page: goal, scope, non-goals, acceptance criteria, and open questions with proposed defaults.

## Consequences

- Acceptance criteria are the check for the plan and the PR. Each backend criterion needs a test that fails before the change and passes after it.
- Open questions get a default so work isn't blocked. The owner can overrule a default in the issue.
- Issues stay open until the feature ships. Close them from the PR that delivers the last criterion.
- Deferred: a `requirements` label or issue template, until we have more than one or two issues.
- First use: [#11, time machine MVP](https://github.com/sShaAanGg/3js/issues/11).
