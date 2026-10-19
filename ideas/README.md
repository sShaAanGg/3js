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
| 8 | [Math and simulation](08-math-and-simulation.md) ⏭ **next, after #1** | A small physical thermal model in place of random noise, then prediction from history | S + M | Medium–High |

Effort assumes one person working evenings: **S** ≈ a few weekends, **M** ≈ 1–2 months, **L** ≈ a quarter or more.

## Decision

We chose **#1, Time machine** first because of two constraints: less than 3 GB of free RAM, and Java first. The reasoning is recorded in [docs/decisions/0001-time-machine-first.md](../docs/decisions/0001-time-machine-first.md).

**#8, Math and simulation** comes next, once the time machine ships: first the thermal model, then prediction. See [docs/decisions/0004-math-after-time-machine.md](../docs/decisions/0004-math-after-time-machine.md).

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
 8 Math/simulation ── thermal model replaces SensorSimulator; prediction reads the log
```

## My recommendation

*(Written before the two limits came up. See [decision 0001](../docs/decisions/0001-time-machine-first.md) for what changed.)*

1. **Start with 1 (Time machine).** It's the smallest step that changes what the project *is*. The current `asset` table is overwritten every 2 s, so all history is lost. Turning that into an append-only event log unlocks ideas 2, 5 and 7 almost for free. It also makes a great demo: "show me the room 10 minutes before the alarm."
2. **Add 6 (Ambient twin) as a quick win alongside it.** It's mostly frontend work, it's fun, and nobody else does it.
3. **Then choose an audience:** 4 (homelab) if you want real users and real data, or 5 (drills) if you want a strong, unusual product story.

Ideas 2 and 3 are the most technically interesting but also the deepest, so they make good "phase 2" goals.

## Common groundwork (needed by most ideas)

- **Push instead of poll:** replace the 2 s HTTP polling with WebSocket or SSE from Spring, so playback and live updates share one stream.
- **Richer asset model:** add `type`, `parent_id` (room → rack → device), dimensions, and free-form `attributes JSONB`. Today an asset is just a shape, a color and a temperature.
- **Real geometry:** racks, rows and rooms instead of primitive shapes, using instanced meshes so a few thousand devices still render smoothly.
