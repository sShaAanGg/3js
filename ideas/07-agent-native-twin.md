# 7. Agent-native twin

> The twin as a tool AI agents use, with their actions shown in 3D.

## The idea

Many tools are adding "ask the chatbot about your dashboard". The more interesting angle is the other direction: make the twin a **first-class tool for AI agents** (via an MCP server or a plain tool API), and make the 3D view the place where humans **watch and approve** what agents do.

- An agent investigating an alert calls `get_asset`, `get_neighbors`, `get_history`, `simulate_move`. While it works, the camera follows its attention: the assets it's looking at glow, and its reasoning shows as notes pinned in 3D space.
- Proposed changes (move this VM, power-cap this rack) appear as a **pending branch** ([idea 2](02-fork-the-room.md)) that a human reviews visually and approves or rejects.
- Natural-language camera control is the easy first step: "show me everything above 40 °C in row B".

## Why it's different

It's less differentiated than the others, because LLM features are everywhere now. The distinctive part is *spatial visibility into agent behavior*: seeing where an agent looked and what it wants to change, in the physical context, before it touches anything.

## How it builds on what exists

- The REST API in `AssetController` is already a reasonable tool surface. Wrap it as an MCP server (Spring AI has MCP support, or use a small sidecar).
- Needs the event log (idea 1) to attribute changes to "agent X", and branches (idea 2) for the approval flow to be meaningful.

## MVP

1. MCP server exposing read-only tools.
2. A "focus" channel: the agent's tool calls highlight assets in the live UI.
3. Text box for natural-language queries → filter + camera move.

## Risks / open questions

- Hype risk: this is easy to make gimmicky. It's only worth it if the visual-review part is genuinely useful.
- Write access by agents must go through branches + human approval, never straight to `main`.
