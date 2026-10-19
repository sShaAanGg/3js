# 8. Math and simulation

> Replace the random sensor noise with a small physical model, then predict from it.

## The idea

Today `SensorSimulator` adds random noise to each temperature every 2 s. The numbers look alive but mean nothing. This idea brings real math into the twin in two steps, each useful on its own:

- **A. Thermal model:** each device has a heat output, a thermal mass and a cooling rate, and it warms its neighbours. Together that is a system of coupled ODEs, `dT/dt = A·T + b`. Stepping it forward replaces the random walk. A fan failure becomes a change to `A`, and the room heats up the way a real one would.
- **C. Smoothing and prediction:** fit the recorded history with least squares, then run a Kalman filter to smooth noisy readings and forecast them ("this rack reaches 40 °C in about 12 min").

The math you'd learn: matrices and their eigenvalues (the room's time constants), explicit vs. implicit Euler (an implicit step solves a linear system), least squares (normal equations, QR), and the Kalman filter (state vector and covariance matrices).

Every value the model produces is **labelled as simulated**, in the API and in the UI. The model's coefficients are made up, so it shows behaviour, not truth.

## Why it's different

Commercial tools either show raw readings or run heavy CFD. A small, explainable model you can read in one file sits in between. It is believable enough to drive demos and drills, and simple enough to check by hand.

## How it builds on what exists

- **A** replaces the random walk in `SensorSimulator` with a model step. It's plain Java with `double[]` arrays: no numerics library, almost no RAM.
- **C** reads the history table from [idea 1](01-time-machine.md), so it comes after the time machine.
- Later: the same model on a floor grid becomes the heat map in [idea 2](02-fork-the-room.md), and scripted failures feed [idea 5](05-drill-mode.md).

## MVP

1. A: thermal model in the backend, with a `simulated` flag on every reading it writes.
2. A: a test that checks the model against a case solved by hand (one device cooling exponentially).
3. C: a least-squares trend line plus a forecast for one asset, shown on the timeline.
4. C: a Kalman filter in place of the trend line once 3 works.

## Not in scope

- **B, heat-diffusion floor map:** wait for idea 2.
- **D, dependency-graph criticality:** wait for idea 3.
- **E, a "show the math" overlay of Three.js transforms:** a learning toy, not part of the twin.
- Heavy numerics libraries (ND4J, Commons Math across the codebase). At this scale hand-written code, or EJML if needed, is enough, and it stays within the RAM limit.

## Risks / open questions

- Made-up coefficients can pass for real data, so the `simulated` label must survive every path: the API, history, permalinks, exports.
- Explicit Euler goes unstable if the time step is long compared with the fastest time constant. Use implicit Euler, or check the step against the eigenvalues.

Decision: [0004, math and simulation after the time machine](../docs/decisions/0004-math-after-time-machine.md).
