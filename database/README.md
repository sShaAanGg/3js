# Database (PostgreSQL)

PostgreSQL 16 runs in the `db` service. Its data lives in the `db-data` volume, and `init.sql` in this folder creates the schema and seed data the first time that volume is created.

## Connecting

Open a `psql` shell inside the container:

```bash
docker compose exec db psql -U twin -d digitaltwin
```

Use the values from `.env` if you changed them. Type `\q` to exit.

Useful `psql` commands:

| Command | What it does |
|---------|--------------|
| `\dt` | List tables |
| `\d asset` | Show the columns of the `asset` table |
| `\l` | List databases |
| `\x` | Toggle expanded output (easier to read wide rows) |
| `\timing` | Show how long each query takes |

From your own machine, the database is also exposed on `localhost:5432`, so a GUI client such as DBeaver, pgAdmin or DataGrip works with the same user, password and database name.

## Useful queries

```sql
SELECT id, name, shape, status, temperature, updated_at FROM asset ORDER BY id;

-- Assets that are currently in trouble
SELECT name, temperature, status FROM asset WHERE status <> 'OK';

-- Move an asset (the simulator doesn't touch positions).
-- Note: a direct UPDATE is not recorded in history; use PUT /api/assets/{id}/position for that.
UPDATE asset SET pos_x = 3, pos_z = -2 WHERE id = 1;

-- History: the last 20 recorded events
SELECT id, asset_id, recorded_at, kind, status, temperature
FROM asset_event ORDER BY id DESC LIMIT 20;

-- How big history has grown
SELECT count(*), min(recorded_at), max(recorded_at),
       pg_size_pretty(pg_total_relation_size('asset_event')) AS size
FROM asset_event;
```

Changes made with `UPDATE` appear in the 3D view on its next poll, within about 2 seconds. The simulator keeps changing temperatures, so `status` and `temperature` can differ between two queries.

## Checking logs

```bash
docker compose logs -f db
```

Postgres logs connection attempts, startup and init-script progress there. If it won't start, look here first.

## Resetting data

Re-running `init.sql` requires an empty volume. To reset:

```bash
docker compose down -v
docker compose up --build
```

This deletes every row, including any you added. To keep your data and only change the schema, write a migration instead of resetting. (The project doesn't have a migration tool yet.)

## Schema

Both tables are defined in [`init.sql`](init.sql):

- `asset`: the current state of each asset.
- `asset_event`: history for the time machine. Each row is a full snapshot of one asset at one moment. The backend writes it, and a retention job trims it (see the [backend guide](../backend/README.md#history-time-machine)).

The backend's JPA entities, `Asset.java` and `AssetEvent.java`, map to them, and Hibernate's `ddl-auto` is `none`. This means Hibernate never changes the table. If you add a column in SQL, add the matching field to the entity too, or the backend will fail when it reads the table.
