# Backend (Spring Boot)

Java 21 Spring Boot 3.5 app, built with Maven. It serves the REST API under `/api/assets` and runs the sensor simulator. Source is in `src/main/java/com/example/digitaltwin/`.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/assets` | All assets |
| GET | `/api/assets/{id}` | One asset (404 if missing) |
| PUT | `/api/assets/{id}/position` | Move an asset; body `{"x":..,"y":..,"z":..}` |
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

The project has no tests yet, so `mvn package` only checks that the code compiles.
