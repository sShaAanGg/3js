---
name: frontend-build
description: Type-check and build the Angular/Three.js frontend. Use after any change under frontend/.
---

# Frontend build

1. On WSL, `npm` may be the Windows shim (`/mnt/c/...`). Use a Linux Node 22 instead:
   ```bash
   curl -fsSL https://nodejs.org/dist/v22.20.0/node-v22.20.0-linux-x64.tar.xz | tar -xJ -C <scratch>/node --strip-components=1
   cd frontend && PATH=<scratch>/node/bin:/usr/bin:/bin npm install --no-audit --no-fund && npx ng build
   ```
2. `ng build` is the check: `strictTemplates` is on, so template and type errors fail the build. Bundle budgets are a warning at 1 MB and an error at 2 MB (`angular.json`).
3. `package-lock.json` and `node_modules/` are git-ignored, so nothing extra should show in `git status`.
4. A successful build doesn't prove the UI works. If you couldn't open it in a browser, say so in the PR and list what the reviewer should try by hand.

Keep frontend changes small and commented. The owner is more at home in Java than in Angular/Three.js, so prefer putting logic in the backend (e.g. `?at=` returns the same JSON as the live endpoint).
