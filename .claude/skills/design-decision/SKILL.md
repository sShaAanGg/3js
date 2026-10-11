---
name: design-decision
description: Record why a design option was chosen over others. Use after any discussion that picks a direction, pattern or implementation.
---

# Design decision

1. Ask **why** before writing: why this option, why not the others? If the owner's reason is missing or weak, ask, and point out anything that seems unreasonable.
2. Check the choice against the standing constraints:
   - less than 3 GB of free RAM on one laptop (no extra services; cap the JVM, Postgres and containers)
   - Java first, lighter on Angular/Three.js (logic in Spring, small frontend changes)
3. Write a short record in `docs/decisions/NNNN-<slug>.md` and add it to the table in `docs/decisions/README.md`:
   ```markdown
   # NNNN. <Decision> (<YYYY-MM-DD>)
   ## Context        what problem, which constraints
   ## Options        each option in one line
   ## Decision and why  why this one beat the others
   ## Consequences   what it costs, what it unlocks, what we deferred
   ```
4. Link it from the related docs (e.g. `ideas/…`, a component README) and from the PR.
5. Follow it up with `potential-issue` for every risk you noticed.

Example: the time machine (idea #1) was chosen because history fits in the existing Postgres (low RAM), and almost all the work is Spring/SQL (Java first).
