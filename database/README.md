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

-- Move an asset (the simulator doesn't touch positions)
UPDATE asset SET pos_x = 3, pos_z = -2 WHERE id = 1;
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

The `asset` table is defined in [`init.sql`](init.sql). The backend's JPA entity, `Asset.java`, maps to it, and Hibernate's `ddl-auto` is `none`. This means Hibernate never changes the table. If you add a column in SQL, add the matching field to `Asset.java` too, or the backend will fail when it reads the table.
