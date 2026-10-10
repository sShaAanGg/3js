# 5. Drill mode

> Game-style fault injection to train people and test runbooks.

## The idea

Pilots train in simulators. Data center and plant operators mostly train on the job, during real incidents. Drill mode turns the twin into a **flight simulator for operations**:

- An instructor (or a script) injects a scenario: "CRAC-2 fails at T+0, the inlet temperature in row B climbs, a PDU breaker trips at T+4 min."
- The trainee sees the twin react in real time and must respond: find the cause, follow the runbook, take actions in the UI (fail over, power off, move load).
- Afterwards you get a **score and a replay**: time to detect, time to mitigate, and wrong turns. The replay comes from [idea 1](01-time-machine.md).
- Scenarios are files (YAML) that people can write, share and version, like test cases for humans.

There's also a chaos-engineering angle: run the same scenario against the dependency graph from [idea 3](03-blast-radius.md) to check whether the runbook actually works.

## Why it's different

DCIM shows real state. Digital twins mostly do monitoring or design. Hardly anything uses the twin for **practicing**, and it's a natural fit for a 3D, game-like frontend. It's also a fun thing to build: timelines, scoring, UI juice.

## How it builds on what exists

- `SensorSimulator` already fakes the world. Generalize it into a **scenario engine** that drives temperatures and statuses from a script instead of random walks.
- A drill is a sandboxed branch (see [idea 2](02-fork-the-room.md)), or just a separate in-memory world, so it never touches real state.
- UI: alarm sounds, a countdown, an action panel, and a debrief screen.

## MVP

1. Scenario YAML format + engine (timed events: set temperature curve, set status, fail asset).
2. "Start drill" button that swaps the live state for the scenario.
3. A minimal action set (acknowledge, isolate asset, mark resolved).
4. Debrief with timings.

## Risks / open questions

- The scoring rubric needs domain input. Start with time-based metrics only.
- Realism vs. effort: hand-authored curves are fine, and no physics is needed.
