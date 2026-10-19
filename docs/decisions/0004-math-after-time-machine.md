# 0004. Math and simulation: thermal model then prediction, after the time machine (2026-10-19)

## Context

The owner wants to bring math (linear algebra, simulation) into the project. `SensorSimulator` currently writes random noise, so the data looks alive but means nothing. Constraints: less than 3 GB of free RAM, Java first, and the time machine ([0001](0001-time-machine-first.md), [#11](https://github.com/sShaAanGg/3js/issues/11)) is still in draft PRs.

## Options

- A. Thermal model: coupled ODEs `dT/dt = A·T + b` in place of the random walk.
- B. Heat-diffusion floor map: Laplacian and iterative solvers in the frontend.
- C. Smoothing and prediction: least squares and a Kalman filter over the history.
- D. Dependency-graph criticality: adjacency matrix and eigenvector centrality.
- E. A "show the math" overlay of Three.js transforms.

Order: math before or after finishing the time machine.

## Decision and why

**A, then C, after the time machine.** The owner's goal is a more *believable* twin, not math for its own sake. A gives the data physical meaning, and C makes real predictions from it. A is plain Java with arrays, so it is Java first and costs almost no RAM. B and D depend on ideas 2 and 3. E teaches math but doesn't improve the twin.

The time machine comes first: its PRs are already drafted, C needs its history table, and A's behaviour over time is only worth much when you can replay it.

## Consequences

- No fake physics passed off as real: every model-generated value carries a `simulated` label in the API and the UI.
- No heavy numerics libraries. Hand-written code, or EJML if it is really needed.
- Risk: the `simulated` label has nowhere to live in the schema yet ([#16](https://github.com/sShaAanGg/3js/issues/16)).
- Each step gets its own requirements issue (as in [0002](0002-requirements-as-issues.md)) when it starts. None is filed yet.
- Deferred: B (with idea 2), D (with idea 3), E (dropped for now).
- Idea doc: [ideas/08-math-and-simulation.md](../../ideas/08-math-and-simulation.md).
