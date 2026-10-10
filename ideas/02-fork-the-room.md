# 2. Fork the room

> Git-style branches for physical changes, with a rough in-browser heat model.

## The idea

Before you move a rack, add a server or shut down a cooling unit, **fork** the room. In your branch you drag things around in 3D, and a lightweight simulation shows the predicted effect: heat bleeding into neighbors, an aisle getting warmer. Then you compare the branch with `main` side by side or as an overlay. When the work is actually done, you **merge** the branch: the plan becomes reality in the twin. You can also export it as a checklist of work steps.

The simulation is deliberately *approximate and explainable*: a coarse 3D voxel grid with heat sources and diffusion, running in a WebGPU or WebGL compute pass. It's not CFD. It's "good enough to spot an obviously bad idea in two seconds."

## Why it's different

Commercial what-if tools exist, but they rely on heavyweight CFD runs that take minutes to hours and cost a fortune. Branch/compare/merge for physical layouts, with an instant toy-physics preview, is a developer workflow applied to facilities, and that hasn't really been done.

## How it builds on what exists

- Needs the event log from [idea 1](01-time-machine.md). A branch is just a named event stream that forks from a point on `main`.
- `PUT /api/assets/{id}/position` becomes branch-aware: `PUT /api/branches/{b}/assets/{id}/position`.
- Drag-to-move in Three.js (TransformControls) replaces curl for positioning.
- The heat model runs entirely in the frontend, so the backend stays simple.

## MVP

1. Branches stored as event streams, plus a branch switcher in the UI.
2. Drag-to-move assets inside a branch.
3. 2D heat map on the floor plane (start in 2D, go 3D later).
4. Split-screen compare with `main`.

## Risks / open questions

- The heat model can mislead people if they treat it as truth. It needs clear "approximation" labeling and maybe calibration against real readings.
- Merge conflicts: what happens if `main` changed since the fork? Keep it simple at first (refuse, or rebase by re-applying moves).
