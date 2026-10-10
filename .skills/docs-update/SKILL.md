---
name: docs-update
description: Keep READMEs and docs in sync with a change, and get docs-only changes onto main. Use after any change a reader would notice.
---

# Docs update

Which file to touch:

| Change | Update |
|--------|--------|
| New/moved file | Tree in root `README.md` |
| API, settings, logs | Component `README.md` (`backend/`, `frontend/`, `database/`) |
| New failure mode | `docs/troubleshooting.md` (exact error text → fix) |
| New guide | Link it from `docs/README.md` |
| Design reasoning | `design-brain-storming/` (see `design-decision`) |

Check relative links before committing:

```bash
for f in README.md docs/*.md */README.md ideas/*.md; do d=$(dirname "$f")
  grep -o '](\([^)#]*\.md\))' "$f" | sed 's/](\(.*\))/\1/' |
  while read -r l; do [ -f "$d/$l" ] || echo "BROKEN: $f -> $l"; done; done
```

Heading anchors: lowercase, spaces → `-`, punctuation dropped (`## Status: chosen (2026-10-10)` → `#status-chosen-2026-10-10`).

Getting docs onto main: the owner allows docs-only changes without asking, **but** `main` requires a PR with 1 approval, and the author can't approve their own PR. Open a docs PR and tell the owner. Never `gh pr merge --admin` without explicit permission.
