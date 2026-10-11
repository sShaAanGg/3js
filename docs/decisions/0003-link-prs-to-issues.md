# 0003. Link PRs to issues with "Closes #N" in the body (2026-10-11)

## Context

Each feature now has a requirements issue (see [0002](0002-requirements-as-issues.md)), and several PRs can deliver one issue. The link between PR and issue has to be easy to set up and easy to read. It should also close the issue automatically when the work is done.

## Options

1. A `Closes #N` or `Refs #N` line at the top of the PR body. GitHub links the PR and closes the issue on merge.
2. A GitHub Action that checks each PR for an issue reference and fails if it's missing.
3. A Claude Code hook on `gh pr create` that adds the reference. It only runs in local sessions, not in the web UI.

## Decision and why

Option 1. It's built into GitHub, needs no setup, and works for every PR, whichever tool opened it. Option 2 would catch a missing link, but it's extra workflow to maintain for a solo project, and we can add it later if we miss links. Option 3 covers only local sessions, so it would be inconsistent.

## Consequences

- The PR body starts with `Closes #N` for the PR that finishes an issue, and `Refs #N` for a partial one.
- The title starts with `enhancement1 ` for PRs linked to an issue, as used in #13 and #14.
- Deferred: an Action that checks the link, if a link is ever missed.
