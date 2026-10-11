# Skills for LLM agents

Short, repeatable procedures for this repo. Each lives in `<name>/SKILL.md` (frontmatter `name` + `description`), where Claude Code discovers project skills automatically.

| Skill | Use it when |
|-------|-------------|
| [backend-test](backend-test/SKILL.md) | After a backend change, even without Java or Docker |
| [frontend-build](frontend-build/SKILL.md) | After a change under `frontend/` |
| [schema-change](schema-change/SKILL.md) | `init.sql` or a JPA entity changes |
| [docs-update](docs-update/SKILL.md) | Any change a reader would notice; getting docs onto `main` |
| [design-decision](design-decision/SKILL.md) | A discussion picked a direction: record the why |
| [potential-issue](potential-issue/SKILL.md) | You spot a risk: file a GitHub issue labelled `potential` |
| [feature-pr](feature-pr/SKILL.md) | Taking a code change from branch to reviewable PR |

Standing constraint every skill assumes: Java first (lighter on Angular/Three.js). The RAM limit is in [`AGENTS.md`](../../AGENTS.md).
