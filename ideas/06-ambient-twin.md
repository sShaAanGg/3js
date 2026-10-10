# 6. Ambient twin

> A calm wall display plus sound: hear the room before you read it.

## The idea

Monitoring UIs compete for attention with red badges and dense dashboards. Go the opposite way: a twin designed to sit on a wall TV or a second monitor and be **glanceable, beautiful and quiet when things are fine**.

- **Ambient view:** a slow orbiting camera, soft lighting, assets breathing gently with their load. When something drifts toward WARNING, the camera eases over to it and lingers. When all is well, it's basically a screensaver.
- **Sonification:** each asset contributes a soft tone to a generative ambient soundscape (Web Audio API). Pitch follows temperature, and rhythm follows load. A healthy room sounds harmonious. A problem shows up as a dissonant note you *notice without looking*, which is the same reason a mechanic listens to an engine.
- **Daily digest view:** a time-lapse of the last 24 hours compressed into 30 seconds. It reuses [idea 1](01-time-machine.md) if available.

## Why it's different

There's no real precedent in DCIM or twin tooling, and there's research behind it: sonification is used for monitoring in hospitals and network operations centers because hearing handles background patterns well. It's also the most "side project"-flavored idea here, with lots of creative freedom.

## How it builds on what exists

- Pure frontend on top of today's `/api/assets` polling. No backend changes needed.
- The WARNING glow and ALARM pulse already exist in `twin-scene.component.ts`. Ambient mode is a different camera + material + audio layer over the same data.

## MVP

1. `/ambient` route: auto-orbit camera, softer materials, no UI chrome.
2. Web Audio: one oscillator per asset mapped to temperature, through a reverb.
3. A "drift to attention" camera move when the status changes.

## Risks / open questions

- Sound design is subjective. Keep it optional, with a mute switch and presets.
- Browsers block autoplaying audio, so it needs one click to start.
