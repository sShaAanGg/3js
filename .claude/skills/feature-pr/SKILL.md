---
name: feature-pr
description: Take a code change from branch to a reviewable pull request. Use for any change that touches code (not docs-only).
---

# Feature PR

1. Branch from `origin/main` (`feature/<slug>`). Never push code to `main`.
2. Build and test with `backend-test` and/or `frontend-build`. For schema work, also follow `schema-change`.
3. Update docs in the same PR (`docs-update`), and record the why (`design-decision`).
4. Commit as the git user configured in the repo; don't override the author.
5. PR body sections:
   - **What changes**: backend / frontend / ops, a few bullets each.
   - **Review guide**: the files in the order to read them.
   - **⚠️ Before running**: e.g. `docker compose down -v` after a schema change.
   - **Testing**: what ran, with counts, and **Not tested**: what didn't, plus what to try by hand.
6. Use absolute URLs in PR bodies (relative links resolve against `/pull/`).
7. File every risk you noticed as a `potential-issue`. Don't merge: the owner reviews.
