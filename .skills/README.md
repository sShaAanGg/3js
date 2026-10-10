# Skills for LLM agents

Short, repeatable procedures for this repo. Each lives in `<name>/SKILL.md`, in the format Claude Code uses (frontmatter `name` + `description`). To let Claude Code discover them automatically, link the folder: `mkdir -p .claude && ln -s ../.skills .claude/skills`.

| Skill | Use it when |
|-------|-------------|
| [backend-test](backend-test/SKILL.md) | After a backend change, even without Java or Docker |
| [frontend-build](frontend-build/SKILL.md) | After a change under `frontend/` |
| [schema-change](schema-change/SKILL.md) | `init.sql` or a JPA entity changes |
| [docs-update](docs-update/SKILL.md) | Any change a reader would notice; getting docs onto `main` |
| [design-decision](design-decision/SKILL.md) | A discussion picked a direction: record the why |
| [potential-issue](potential-issue/SKILL.md) | You spot a risk: file a GitHub issue labelled `potential` |
| [feature-pr](feature-pr/SKILL.md) | Taking a code change from branch to reviewable PR |

Standing constraints every skill assumes: less than 5 GB of free RAM on one laptop, and Java first (lighter on Angular/Three.js). The rules for agents are in [`MEMORY.md`](../MEMORY.md).
