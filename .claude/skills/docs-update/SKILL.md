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
| Design reasoning | `docs/decisions/` (see `design-decision`) |

Check relative links before committing:

```bash
for f in *.md docs/*.md docs/*/*.md */README.md ideas/*.md .claude/skills/*.md; do d=$(dirname "$f")
  grep -o '](\([^)#]*\.md\))' "$f" | sed 's/](\(.*\))/\1/' |
  while read -r l; do [ -f "$d/$l" ] || echo "BROKEN: $f -> $l"; done; done
```

Heading anchors: lowercase, spaces → `-`, punctuation dropped (`## Status: chosen (2026-10-10)` → `#status-chosen-2026-10-10`).

Getting docs onto main: every change goes through a PR, docs included (`main` rejects direct pushes). Open the PR and tell the owner, who merges. Never use `gh pr merge --admin`.
