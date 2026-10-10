# Ideas: where this project could go

These are product directions, not a backlog. Each one tries to be *different* from what classic DCIM tools (asset inventory, rack elevations, capacity reports) and enterprise digital-twin suites (BIM/CAD import, CFD, heavy dashboards) already do well. Nothing here is decided yet. Pick one or two, or combine them.

## The ideas at a glance

| # | Idea | One-line pitch | Effort | How different |
|---|------|----------------|--------|---------------|
| 1 | [Time machine](01-time-machine.md) ✅ **chosen** | Scrub, replay and diff the room's history like a video timeline | M | High |
| 2 | [Fork the room](02-fork-the-room.md) | Git-style branches for physical changes, with a rough in-browser heat model | L | High |
| 3 | [Blast radius](03-blast-radius.md) | Click anything and see what fails with it, drawn as a shockwave | M | Medium–High |
| 4 | [Homelab twin](04-homelab-twin.md) | A twin for the rack in your closet: zero-config, auto-discovered | M | High (different audience) |
| 5 | [Drill mode](05-drill-mode.md) | Game-style fault injection to train people and test runbooks | M | High |
| 6 | [Ambient twin](06-ambient-twin.md) | A calm wall display plus sound: hear the room before you read it | S | Very high |
| 7 | [Agent-native twin](07-agent-native-twin.md) | The twin as a tool AI agents use, with their actions shown in 3D | M | Medium (crowded space) |

Effort assumes one person working evenings: **S** ≈ a few weekends, **M** ≈ 1–2 months, **L** ≈ a quarter or more.

## Decision: Time machine first (2026-10-10)

A summary of how we picked a direction, so the reasoning isn't lost.

**1. Proposal.** These seven ideas were written up, with a recommendation to start with #1 (see [My recommendation](#my-recommendation) below).

**2. Two real limits.** After review, two constraints came up. Both are part of the design, not temporary inconveniences:

- **Less than 5 GB of free memory.** The whole stack (Postgres, the Spring Boot JVM, nginx, plus Docker builds) runs on one laptop under WSL2.
- **Java is the strong side, Angular and Three.js less so.** Work that lives in Spring is faster to build, review and debug than frontend or 3D work.

**3. Why both limits point to #1.**

- *Memory:* history fits in the Postgres we already run, with no new service: no Kafka, no TimescaleDB, no separate time-series database. The data is tiny: 4 assets every 2 s is about 170k rows a day, and storing only meaningful changes cuts that a lot. A retention job keeps it bounded. Memory pressure comes from elsewhere: the JVM's default heap sizing, Postgres buffers and the Angular build (1–2 GB briefly). Those are handled with explicit caps, not by avoiding features.
- *Java first:* almost all of #1 is Spring and SQL: an append-only event table, the "state at time T" query, retention, endpoints and tests. The key design choice is that **`GET /api/assets?at=<time>` returns the same JSON as the live endpoint**, so the Three.js scene doesn't need to know about time. The frontend only gains a slider, and 3D-heavy extras (ghosted "before" meshes in compare mode) wait until later.
- *Foundation:* the event log is reused by #2, #5 and #7, so this effort isn't spent on a side branch.

**4. How the limits affect the other ideas.**

- **#6 Ambient twin** is no longer the "quick win alongside": it's almost entirely Three.js and Web Audio, the less familiar side.
- **#2 Fork the room**'s heat model is GPU/frontend-heavy. The branching half is backend work and fits well later.
- **#7 Agent-native twin**: an extra sidecar process costs memory. Building MCP into the Spring app instead would avoid that.
- **#4 Homelab twin** (data adapters in Java) and **#5 Drill mode** (a scenario engine in place of `SensorSimulator`) both suit the Java-first constraint. They remain good next steps.

**5. Outcome.** The first milestone (event table, `?at=` endpoints, retention, a timeline bar and memory caps) is in [PR #1](https://github.com/sShaAanGg/3js/pull/1). Details are in [01-time-machine.md](01-time-machine.md#status-chosen-2026-10-10).

## How they fit together

Several ideas share foundations, so the order matters more than the pick:

```
           ┌──────────────────────────┐
           │ Event log (from idea 1)  │  every change stored as an event, not an UPDATE
           └────────────┬─────────────┘
       ┌────────────────┼──────────────────┬───────────────────┐
       ▼                ▼                  ▼                   ▼
 1 Time machine   2 Fork the room    5 Drill mode      7 Agent-native
 (replay)         (branch events)    (scripted events) (agents emit events)

 3 Blast radius ── needs a dependency graph (new table), independent of the log
 4 Homelab twin ── needs real data adapters instead of SensorSimulator
 6 Ambient twin ── can sit on top of what exists today
```

## My recommendation

*(Written before the two limits came up; see the decision above for what changed.)*

1. **Start with 1 (Time machine).** It's the smallest step that changes what the project *is*. The current `asset` table is overwritten every 2 s, so all history is lost. Turning that into an append-only event log unlocks ideas 2, 5 and 7 almost for free. It also makes a great demo: "show me the room 10 minutes before the alarm."
2. **Add 6 (Ambient twin) as a quick win alongside it.** It's mostly frontend work, it's fun, and nobody else does it.
3. **Then choose an audience:** 4 (homelab) if you want real users and real data, or 5 (drills) if you want a strong, unusual product story.

Ideas 2 and 3 are the most technically interesting but also the deepest, so they make good "phase 2" goals.

## Common groundwork (needed by most ideas)

- **Push instead of poll:** replace the 2 s HTTP polling with WebSocket or SSE from Spring, so playback and live updates share one stream.
- **Richer asset model:** add `type`, `parent_id` (room → rack → device), dimensions, and free-form `attributes JSONB`. Today an asset is just a shape, a color and a temperature.
- **Real geometry:** racks, rows and rooms instead of primitive shapes, using instanced meshes so a few thousand devices still render smoothly.
