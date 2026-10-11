# Digital Twin (Spring Boot + Angular/Three.js + PostgreSQL)

## Project structure

```
3js/
├── backend/                                   # Java Spring Boot app
│   ├── src/main/
│   │   ├── java/com/example/digitaltwin/
│   │   │   ├── DigitalTwinApplication.java    # entry point (enables scheduling)
│   │   │   ├── asset/
│   │   │   │   ├── Asset.java                 # JPA entity for the asset table
│   │   │   │   ├── AssetRepository.java       # Spring Data repository
│   │   │   │   └── AssetController.java       # REST API under /api/assets (?at= for the past)
│   │   │   ├── history/                       # time machine
│   │   │   │   ├── AssetEvent.java            # JPA entity: one asset snapshot at one moment
│   │   │   │   ├── AssetEventRepository.java  # "state at T" and retention queries
│   │   │   │   ├── HistoryService.java        # records changes, rebuilds past state, cleanup
│   │   │   │   ├── HistoryProperties.java     # twin.history.* settings
│   │   │   │   └── HistoryController.java     # REST API under /api/history
│   │   │   └── simulation/
│   │   │       └── SensorSimulator.java       # fake sensor data (temperature/status)
│   │   └── resources/
│   │       └── application.yml                # DB connection, simulator and history settings
│   ├── src/test/java/...                      # JUnit tests (H2 in-memory DB, no Docker needed)
│   ├── README.md                              # API, logs, simulator settings
│   ├── Dockerfile                             # Maven build → JRE 21 runtime
│   ├── .dockerignore
│   └── pom.xml
├── frontend/                                  # Angular + Three.js app
│   ├── src/
│   │   ├── app/
│   │   │   ├── scene/
│   │   │   │   ├── twin-scene.component.ts    # Three.js scene, render loop, API polling, timeline
│   │   │   │   ├── twin-scene.component.html  # canvas + status panel + timeline bar
│   │   │   │   └── twin-scene.component.css
│   │   │   ├── services/
│   │   │   │   └── asset.service.ts           # HTTP client for /api/assets and /api/history
│   │   │   ├── app.component.ts
│   │   │   └── app.config.ts
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.css
│   ├── README.md                              # logs, proxy, browser debugging
│   ├── Dockerfile                             # Node build → nginx runtime
│   ├── nginx.conf                             # serves the app, proxies /api → backend
│   ├── proxy.conf.json                        # /api proxy for `npm start` (local dev)
│   ├── angular.json
│   ├── tsconfig.json
│   ├── tsconfig.app.json
│   ├── .dockerignore
│   └── package.json
├── database/                                  # SQL initialization scripts
│   ├── init.sql                               # schema (asset, asset_event) + seed assets (first DB start)
│   └── README.md                              # DB access, queries, resetting data
├── docs/
│   ├── README.md                              # documentation index
│   ├── troubleshooting.md                     # logs, restarts, cross-service problems
│   └── docker/
│       └── README.md                          # Docker CE install guide (WSL Ubuntu)
├── ideas/                                     # candidate product directions (not decided)
│   └── README.md                              # overview, comparison, recommendation
├── docker-compose.yml                         # orchestrates db + backend + frontend
├── .env.example                               # DB credentials template (copy to .env)
├── .gitignore
└── README.md
```

How the pieces fit together:

- **db** (`postgres:16`) holds the `asset` table (current state) and the `asset_event` table (history). `init.sql` seeds four assets.
- **backend** exposes `GET /api/assets`, `GET /api/assets/{id}` and `PUT /api/assets/{id}/position`.
  `SensorSimulator` changes each asset's temperature every 2 s and sets its status to OK, WARNING (≥35 °C) or ALARM (≥45 °C).
  Meaningful changes are recorded as history. `GET /api/assets?at=<time>` returns the room as it was then (see the [backend guide](backend/README.md#history-time-machine)).
- **frontend** polls `/api/assets` every 2 s and draws each asset as a mesh.
  WARNING assets glow orange and ALARM assets pulse red. Use the mouse to orbit, pan and zoom.
  The **timeline bar** at the bottom scrubs and replays history. The URL gets `?t=<time>`, so you can share a link to a moment.

## Documentation

- [docs/README.md](docs/README.md): index of all guides.
- [docs/troubleshooting.md](docs/troubleshooting.md): logs, restarts and common problems.
- Component guides: [backend](backend/README.md), [frontend](frontend/README.md), [database](database/README.md).
- [ideas/README.md](ideas/README.md): candidate directions for where the project goes next.

## Prerequisites

- Docker Engine with the Compose plugin. On WSL Ubuntu, follow [docs/docker/README.md](docs/docker/README.md).
- For local development without Docker only: JDK 21, Maven, and Node.js 22+.

## Running the stack

```bash
cp .env.example .env          # optional; edit credentials here
docker compose up --build     # add -d to run in the background
```

The first run downloads the base images (Postgres, Maven, Temurin, Node, nginx), so it takes a while.

| URL                                   | What                            |
|---------------------------------------|---------------------------------|
| http://localhost:4200                 | 3D digital twin UI              |
| http://localhost:8080/api/assets      | REST API (JSON)                 |
| http://localhost:8080/actuator/health | Backend health                  |
| `localhost:5432`                      | PostgreSQL (user/pass `twin`)   |

Other useful commands:

```bash
docker compose logs -f backend     # follow backend logs
docker compose down                # stop containers (data kept in the db-data volume)
docker compose down -v             # stop and DELETE the database, so init.sql runs again next time

# Move an asset and watch it move in the UI
curl -X PUT localhost:8080/api/assets/1/position \
     -H 'Content-Type: application/json' -d '{"x":3,"y":0.5,"z":-2}'
```

If something doesn't start or behaves oddly, see [docs/troubleshooting.md](docs/troubleshooting.md).

## Memory limits

The stack is tuned to run on a laptop with less than 3 GB of free RAM. That limit is a deliberate design constraint, and one of the two reasons the time machine was picked as the first feature (see [decision 0001](docs/decisions/0001-time-machine-first.md)). In `docker-compose.yml`:

| Service | `mem_limit` | Notes |
|---------|-------------|-------|
| db | 256 MB | `shared_buffers=64MB`, `max_connections=20` |
| backend | 640 MB | JVM heap capped with `-Xmx256m` in `JAVA_TOOL_OPTIONS` |
| frontend | 64 MB | nginx serving static files |

Builds use more memory than running. The Angular production build in particular can briefly need 1–2 GB. After a backend-only change, rebuild only the backend (`docker compose up --build backend`).

On WSL, you can also cap the whole Linux VM so it can't starve Windows. Create `%UserProfile%\.wslconfig`:

```ini
[wsl2]
memory=3GB
swap=2GB
```

Then run `wsl --shutdown` from Windows and reopen the terminal.

## Developing

Change code and rebuild only the affected service:

```bash
docker compose up --build backend     # after backend changes
docker compose up --build frontend    # after frontend changes
```

Faster loop without rebuilding images:

```bash
docker compose up db                            # just the database
cd backend  && mvn spring-boot:run              # http://localhost:8080
cd frontend && npm install && npm start         # http://localhost:4200, /api proxied to :8080
```

Schema changes go in `database/init.sql`. That script only runs when the database volume is empty, so run `docker compose down -v` to apply them.
