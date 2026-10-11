# 0001. Build the time machine first (2026-10-10)

## Context

Seven product directions were proposed in [`ideas/`](../../ideas/README.md), with a recommendation to start with #1, the time machine. After review, two real limits came up. Both are design constraints, not temporary inconveniences:

- **Less than 3 GB of free memory.** The whole stack (Postgres, the Spring Boot JVM, nginx, plus Docker builds) runs on one laptop under WSL2.
- **Java is the strong side, Angular and Three.js less so.** Work in Spring is faster to build, review and debug than frontend or 3D work.

## Options

1. Time machine: replay and scrub history.
2. Fork the room: branches plus an in-browser heat model.
3. Blast radius: a dependency graph.
4. Homelab twin: real data adapters.
5. Drill mode: scripted fault scenarios.
6. Ambient twin: a calm display plus sound.
7. Agent-native twin: an MCP tool surface.

## Decision and why

Build **#1, the time machine**, first.

- **Memory:** history fits in the Postgres we already run, so there's no new service: no Kafka, no TimescaleDB. The data is tiny: 4 assets every 2 s is about 170k rows a day, and storing only meaningful changes cuts that a lot. A retention job keeps it bounded. Memory pressure really comes from the JVM's default heap, Postgres buffers and the Angular build (1–2 GB briefly). Those are handled with explicit caps.
- **Java first:** almost all of the work is Spring and SQL: the event table, the "state at time T" query, retention, endpoints and tests. `GET /api/assets?at=<time>` returns the same JSON as the live endpoint, so the Three.js scene doesn't need to know about time. The frontend only gains a slider.
- **Foundation:** the event log is reused by #2, #5 and #7.

Why not the others, under these limits:

- **#6 Ambient twin:** almost entirely Three.js and Web Audio, the less familiar side.
- **#2 Fork the room:** the heat model is GPU/frontend-heavy. The branching half is backend work and fits later.
- **#7 Agent-native twin:** a sidecar process costs memory. MCP inside the Spring app would avoid that.
- **#4 Homelab twin** and **#5 Drill mode** suit the Java-first constraint and remain good next steps.

## Consequences

- Memory caps per container, `-Xmx256m` for the JVM, small Postgres buffers, a Hikari pool of 5.
- 3D-heavy extras (compare mode with ghosted meshes) are deferred.
- The first milestone is in [PR #1](https://github.com/sShaAanGg/3js/pull/1). Plan details are in [`ideas/01-time-machine.md`](../../ideas/01-time-machine.md#status-chosen-2026-10-10).
- Known risks are filed as issues #4–#8 (label `potential`).
