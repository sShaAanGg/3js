# 1. Time machine

> Scrub, replay and diff the room's history like a video timeline.

## The idea

Most DCIM tools show *now* plus some charts of the past. Here the whole 3D scene becomes time-addressable. A timeline bar at the bottom lets you drag back to any moment and watch the room exactly as it was: temperatures, statuses, positions, which assets existed. Press play to watch an incident unfold at 10× speed.

On top of that:

- **Diff mode:** pick two timestamps and the scene highlights what changed (moved assets, new assets, assets that got hotter), as ghosted "before" meshes next to solid "after" ones.
- **Permalinks:** `/?t=2026-10-10T14:03:00Z&cam=...` opens the room at that moment and camera angle. You can paste it into a postmortem or chat.
- **Event markers:** alarms, moves and config changes show up as ticks on the timeline. Click one to jump there.

## Why it's different

Charts tell you *a value* changed. Spatial replay tells you *where* it started and how it spread. That's how people actually debug a hot spot. Git-style history for a physical space is rare outside very expensive suites.

## How it builds on what exists

- Today `SensorSimulator` and `PUT /position` **overwrite** the `asset` row, so history is lost.
- Add an append-only `asset_event` table (`asset_id, ts, kind, payload JSONB`). Keep `asset` as the "current state" projection, or derive it from the events.
- Keep it small: store a reading only when it changes by more than a threshold. Each event holds the asset's full state, so seeking to any time means "latest event per asset before T", with no replaying of deltas. TimescaleDB is a drop-in Postgres extension if volume ever grows.
- New endpoints: `GET /api/assets?at=<ts>` (same JSON as today) and `GET /api/history/events?from=&to=`.
- Frontend: `twin-scene.component.ts` already maps assets to meshes, so it just needs a state source that can be "live" or "playback".

## MVP

1. Event table, with the simulator writing events.
2. `state?at=` endpoint.
3. Timeline slider + play/pause in the UI.
4. Permalink with `t=`.

Diff mode and event markers come next.

## Status: chosen (2026-10-10)

This is the direction we're building first. Two constraints shape the design:

- **Under 5 GB of RAM** (one laptop running the whole stack): history lives in the existing Postgres, with no extra services. The JVM and Postgres get explicit memory caps, and a retention job keeps the table small.
- **Java first, lighter on Angular/Three.js:** the logic lives in Spring. `GET /api/assets?at=<time>` returns exactly the same JSON as the live endpoint, so the 3D scene doesn't change. The frontend only gains a timeline bar.

First milestone:

1. `asset_event` table. Each row is a full snapshot of one asset at one moment, written only when something meaningful changes (a move, a status change, or a temperature change ≥ 0.5 °C).
2. `GET /api/assets?at=`, `GET /api/history/range` and `GET /api/history/events`, with tests.
3. Retention set in `application.yml`. The cleanup keeps the newest snapshot of each asset, so any time inside the window can still be rebuilt.
4. Memory caps in `docker-compose.yml`.
5. Timeline slider, play/pause and `?t=` permalinks in the UI.

Later: compare mode, event markers on the timeline, push updates instead of polling.

## Risks / open questions

- Storage growth: needs a retention policy or downsampling.
- Do we model "who changed it"? It's cheap to add now and painful to add later.
