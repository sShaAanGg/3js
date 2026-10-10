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
- Compress readings: store a reading only on change (delta > threshold), plus periodic keyframes, so replay can seek fast. TimescaleDB is a drop-in Postgres extension if volume grows.
- New endpoints: `GET /api/state?at=<ts>` (keyframe + deltas) and `GET /api/events?from=&to=`.
- Frontend: `twin-scene.component.ts` already maps assets to meshes, so it just needs a state source that can be "live" or "playback".

## MVP

1. Event table, with the simulator writing events.
2. `state?at=` endpoint.
3. Timeline slider + play/pause in the UI.
4. Permalink with `t=`.

Diff mode and event markers come next.

## Risks / open questions

- Storage growth: needs a retention policy or downsampling.
- Do we model "who changed it"? It's cheap to add now and painful to add later.
