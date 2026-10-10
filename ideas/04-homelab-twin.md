# 4. Homelab twin

> A twin for the rack in your closet: zero-config, auto-discovered, actually fun.

## The idea

Change the audience. DCIM is built for enterprises with procurement processes. Meanwhile there's a huge, enthusiastic homelab / self-hosting community (r/homelab, r/selfhosted) running Proxmox, TrueNAS, Unraid, Raspberry Pi clusters and UPSes. They love dashboards, but nothing gives them a **3D model of their own setup**.

- `docker compose up`, enter your Proxmox / Prometheus / Home Assistant URLs, and the twin **discovers** hosts, VMs, containers, disks and UPS state.
- A small library of rack-mountable models (1U server, NAS, switch, Pi, mini PC, UPS) to drag into a 10" or 19" rack, or onto a shelf, because a lot of homelabs live on IKEA furniture.
- Show-off features: a shareable read-only link, a "rack card" image export for Reddit posts, power draw → monthly electricity cost.

## Why it's different

It's a different market, not a different feature. It's small enough to finish as a side project, real users would give feedback quickly, and you'd be running against real data instead of `SensorSimulator`.

## How it builds on what exists

- `SensorSimulator` already says "replace with an MQTT/OPC-UA/Kafka consumer". Make that a pluggable **adapter** interface, with the simulator as one adapter and Prometheus, Proxmox, Home Assistant (MQTT) and NUT (UPS) as others.
- The existing Docker Compose setup is already the right distribution format for this audience.
- Needs the richer asset model and real rack geometry.

## MVP

1. Adapter interface + Prometheus adapter (node_exporter gives CPU temp, load, power).
2. Rack/shelf layout editor with a handful of models.
3. Auto-match discovered hosts to placed models.
4. Read-only share link.

## Risks / open questions

- Model library effort: start with parametric boxes (width, height in U, front-panel texture) rather than detailed meshes.
- Security of a public share link: read-only, no hostnames or IPs unless the user opts in.
