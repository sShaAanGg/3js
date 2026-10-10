---
name: potential-issue
description: File a possible problem as a GitHub issue labelled "potential". Use whenever you spot a risk, edge case or weak assumption, in your own work too.
---

# Potential issue

One problem per issue. New issues are already open, so the label is all you need to set:

```bash
gh issue create --label potential --title "<short problem statement>" --body-file body.md
```

Body template:

```markdown
## What could go wrong
<one or two sentences, with the concrete scenario>

## How to reproduce / observe
<steps, command, or "not reproduced yet: reasoning only">

## Possible fixes
- <option>: <trade-off>

## How we'd know it's fixed
<a test that fails now and passes after the fix>
```

Before filing, search for duplicates: `gh issue list --label potential --search "<keywords>"`. Link the issue from the PR or design record that surfaced it.
