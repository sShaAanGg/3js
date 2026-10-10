# Troubleshooting

Problems that involve more than one container, or that happen before any container starts. For a single component, see its own guide: [backend](../backend/README.md), [frontend](../frontend/README.md), [database](../database/README.md).

## Checking what's running

```bash
docker compose ps                     # state and health of db, backend, frontend
docker compose logs -f                # logs from all containers (Ctrl+C to stop following)
docker compose logs -f backend        # logs from one service
docker compose logs --tail=100 db     # last 100 lines only
```

The containers also have fixed names (`twin-db`, `twin-backend`, `twin-frontend`), so `docker logs twin-backend` works too.

If `docker compose ps` shows `unhealthy` or `starting` for `db`, read its logs first. The backend waits for `db` to report healthy before it starts.

## Restarting

| Situation | Command |
|-----------|---------|
| Restart one service | `docker compose restart backend` |
| Rebuild one service after a code change | `docker compose up -d --build backend` |
| Stop everything, keep data | `docker compose down` |
| Stop everything and wipe the database | `docker compose down -v` |

Changes to `.env` take effect only after `docker compose up -d` (or `down` then `up`) recreates the containers.

## Common problems

### Docker commands fail with "Cannot connect to the Docker daemon"

Docker isn't running. On WSL:

```bash
sudo systemctl status docker
sudo systemctl start docker
```

If `systemctl` itself fails, systemd isn't enabled in WSL. See [Enable systemd](docker/README.md#1-enable-systemd-wsl-only).

### "permission denied" on `/var/run/docker.sock`

Your user isn't in the `docker` group yet, or the shell predates adding it. Run `newgrp docker` or log out and back in.

### A port is already in use

The stack uses ports 4200 (frontend), 8080 (backend) and 5432 (database). If one is taken, `docker compose up` fails with `port is already allocated`. Find the owner:

```bash
sudo ss -ltnp | grep -E ':(4200|8080|5432)\b'
```

Stop that process, or change the host side of the matching `ports:` entry in `docker-compose.yml`. For example, `"8081:8080"` moves the backend to port 8081.

### A build fails or uses stale dependencies

Rebuild without the cache:

```bash
docker compose build --no-cache backend     # or frontend
docker compose up -d backend
```

Local `node_modules/` and `target/` folders are excluded from the images by `.dockerignore`, so they don't affect container builds.

### Backend can't connect to the database

Check that the backend's URL uses the service name `db`, not `localhost`, inside Docker. This is set in `docker-compose.yml` as `SPRING_DATASOURCE_URL`. Then check the database is healthy with `docker compose ps db`.

If you run the backend on your machine instead of in Docker, `localhost:5432` is correct, and the defaults in `application.yml` already point there.

### The database password or schema doesn't match what I changed

Postgres reads the password and runs `database/init.sql` only when the data volume is empty. Changing `.env` or `init.sql` has no effect on an existing volume. To apply the change, delete the volume:

```bash
docker compose down -v
docker compose up --build
```

This erases all data in the database.

### The UI loads but shows "Backend unreachable"

The frontend's nginx forwards `/api/` to `http://backend:8080`. Work through this in order:

1. Check the backend is up: `docker compose ps backend`, then `curl http://localhost:8080/actuator/health`.
2. If the health check works from your machine but the UI still fails, check the frontend's proxy logs: `docker compose logs --tail=50 frontend`.
3. Open the browser dev tools (F12), go to the Network tab, and look at the failing `/api/assets` request. Its status code and response body usually say what's wrong.

### The asset list is empty

`init.sql` seeds four assets, but only on a fresh volume. If you deleted rows by hand, restore them by resetting the volume with `docker compose down -v`, or re-insert them using the queries in the [database guide](../database/README.md).
