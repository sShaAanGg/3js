# Backend (Spring Boot)

Java 21 Spring Boot 3.5 app, built with Maven. It serves the REST API under `/api/assets` and `/api/history`, runs the sensor simulator, and records history for the time machine. Source is in `src/main/java/com/example/digitaltwin/`.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/assets` | All assets |
| GET | `/api/assets?at=2026-10-10T14:03:00Z` | All assets as they were at that moment (same JSON shape; time in ISO-8601 UTC) |
| GET | `/api/assets/{id}` | One asset (404 if missing) |
| PUT | `/api/assets/{id}/position` | Move an asset; body `{"x":..,"y":..,"z":..}` |
| GET | `/api/history/range` | `{"earliest":..,"latest":..}`: the span the timeline can show |
| GET | `/api/history/events?from=..&to=..` | Recorded events, oldest first. Defaults to the last hour, max 1000 |
| GET | `/actuator/health` | Health check (the only actuator endpoint exposed) |

## Logs

In Docker:

```bash
docker compose logs -f backend
```

Local run: logs appear in the terminal where you ran `mvn spring-boot:run`.

The simulator logs nothing on each tick, so quiet logs are normal. Look for a `Started DigitalTwinApplication` line to confirm startup. Errors from failed database calls appear as stack traces, and the first line of each usually names the cause.

To see more detail, set the log level with an environment variable on the `backend` service in `docker-compose.yml`:

```yaml
environment:
  LOGGING_LEVEL_COM_EXAMPLE_DIGITALTWIN: DEBUG
  # SQL statements:
  # LOGGING_LEVEL_ORG_HIBERNATE_SQL: DEBUG
```

Then run `docker compose up -d backend`. Revert it afterwards, as the output gets noisy.

## Checking it's alive

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

A `DOWN` status or no response means the app failed to start or can't reach the database. Check the logs.

## Simulator

Settings are in `src/main/resources/application.yml` under `twin.simulation`:

- `enabled`: turn the simulator on or off.
- `interval-ms`: how often temperatures change (default 2000).

Every setting can also be overridden with an environment variable, for example `TWIN_SIMULATION_ENABLED=false`, which is handy for debugging when you want the values to stay still. Add it under `backend.environment` in `docker-compose.yml`, then run `docker compose up -d backend`.

The thresholds for WARNING (35 °C) and ALARM (45 °C) are constants in `SensorSimulator.java`, not settings.

## History (time machine)

Code is in `history/`. Every meaningful change is stored in the `asset_event` table as a full snapshot of the asset. To rebuild the room at time T, the backend takes the newest snapshot per asset recorded at or before T.

Most of the time machine lives here in the backend on purpose. `?at=` returns the same JSON as the live endpoint, so the 3D frontend barely changes, and history uses the existing Postgres instead of a new service. Both choices follow from the project's two constraints: Java first, and a laptop with less than 5 GB of free RAM. See [decision 0001](../docs/decisions/0001-time-machine-first.md).

What gets recorded (`HistoryService`):

- `CREATED`: the first event for an asset. On startup, any asset without history gets one.
- `READING`: the temperature moved at least `min-temperature-delta` since the last stored event. Smaller wobbles are skipped to keep the table small.
- `STATUS_CHANGED`: OK / WARNING / ALARM changed.
- `MOVED`: the asset was moved through `PUT /api/assets/{id}/position`. A direct SQL `UPDATE` bypasses the backend, so it isn't recorded.

Settings under `twin.history` in `application.yml`:

- `min-temperature-delta`: default `0.5` °C.
- `retention`: how long history is kept, default `7d`. Accepts `12h`, `30d` and so on.
- `cleanup-interval-ms`: how often old history is removed (default hourly). Cleanup keeps the newest event per asset from before the cutoff, so any time inside the retention window can still be shown in full.

Try it:

```bash
curl localhost:8080/api/history/range
curl "localhost:8080/api/assets?at=$(date -u -d '5 minutes ago' +%Y-%m-%dT%H:%M:%SZ)"
curl "localhost:8080/api/history/events?from=$(date -u -d '10 minutes ago' +%Y-%m-%dT%H:%M:%SZ)"
```

## Memory

In Docker the JVM runs with `-Xmx256m` and a 640 MB container limit (see `JAVA_TOOL_OPTIONS` in `docker-compose.yml`). The connection pool is capped at 5 (`spring.datasource.hikari.maximum-pool-size`). If you see `java.lang.OutOfMemoryError` in the logs, raise `-Xmx` and `mem_limit` together.

## Running outside Docker

Useful for debugging with a local debugger attached. It needs JDK 21 and Maven, and the database still runs in Docker:

```bash
docker compose up -d db
cd backend
mvn spring-boot:run
```

The defaults in `application.yml` point at `localhost:5432`, so no extra configuration is needed.

## Building and testing

```bash
mvn package              # compile and run tests
mvn spring-boot:run      # run locally
```

Tests don't need Docker or Postgres: repository tests run against an in-memory H2 database, with the schema generated from the entities.

```bash
mvn test                                    # all tests
mvn test -Dtest=HistoryServiceTest          # one class
```

Because the tests use H2, they don't exercise `init.sql`. If you change the schema, update both `init.sql` and the entity.
