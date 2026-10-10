# Instructions for LLMs and agents

Keep these in memory for every session.

1. Record a summary, or at least the key points, of every discussion about a design choice.
    - Create or update a decision record in [`docs/decisions/`](docs/decisions/README.md).
2. Keep asking me WHY we should go for one option and not the others.
    - Add the answer to the decision record if it matters to the design.
3. Look for potential problems, and even create some for us to solve.
    - File them as GitHub issues with the label `potential`.
4. Point out unreasonable choices or decisions in any discussion, so we can improve them.
5. Don't overdo anything without asking me first.
    - For example, planning a feature that touches more than 20 files, or writing something as long as an essay.
    - The point is to maintain lower noise and better abstraction in comments and discussions
6. Make every change through a pull request, docs included. Never push to `main` directly.
    - I'll merge PRs in person.

## Reminders

Some questions to keep in mind, for me and for the agents:

1. Why would we choose something (a design, an implementation, a pattern) over the others?
2. How do we find, reproduce, debug and fix a bug or an error?
3. Based on 2, can we prove that the fix is correct? A fix counts as proven when a test fails before it and passes after it.

- For 2 and 3, document the details in the related GitHub issue.

Repeatable procedures are in [`.claude/skills/`](.claude/skills/README.md).
