# 3. Blast radius

> Click anything and see what fails with it, drawn as a shockwave.

## The idea

Physical layout is only half the story. What matters during an outage is **dependencies**: this PDU feeds these racks, this rack hosts these VMs, those VMs run these services. Model that as a graph and render it *inside* the 3D room:

- Select a PDU, switch or cooling unit, then hit "what if this fails?". A shockwave animates outward along power, network and cooling links. Every affected asset turns red, and a side panel lists affected *services*, not just boxes.
- **Reverse mode:** select a service and see every physical thing it depends on light up in the room, so single points of failure jump out.
- Links render as glowing cables or arcs between assets. They're hidden by default and shown per layer (power / network / cooling / logical).

## Why it's different

DCIM tools often track a power chain in tables. Service-dependency tools (APM, CMDBs) know about software but not the room. Joining the two *spatially and visually* answers the question people actually ask in an incident: "if I unplug this, what dies?"

## How it builds on what exists

- New table `dependency (from_asset, to_asset, kind, redundancy)`. Redundancy matters: an asset with dual PSUs on two feeds survives one failing.
- Graph traversal can be a recursive CTE in Postgres. There's no need for a graph database at this scale.
- Needs the richer asset model (types, logical "service" assets with no position, or a floating position above their host).

## MVP

1. Dependency table plus seed data.
2. `GET /api/assets/{id}/impact` returns the affected set.
3. Lines between assets, and red highlighting for the impact set.
4. Shockwave animation (pure polish, but it's what makes the demo).

## Risks / open questions

- Data entry is the real cost. Who maintains the graph? Auto-discovery (LLDP, hypervisor APIs) helps; see [idea 4](04-homelab-twin.md).
