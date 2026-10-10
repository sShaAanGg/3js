---
name: schema-change
description: Add or change a database table or column. Use whenever database/init.sql or a JPA entity changes.
---

# Schema change

1. Edit `database/init.sql`. It is the only schema source: Hibernate runs with `ddl-auto: none`.
2. Update the matching JPA entity in the same change. A column/field mismatch fails only at runtime.
3. Prefer full-row snapshots and simple queries over clever SQL. Postgres is shared with a low-RAM laptop.
4. Add an index for every new query pattern (e.g. `(asset_id, id)`, `recorded_at`).
5. Existing volumes do **not** rerun `init.sql`. Tell the reader to run `docker compose down -v`, and add the exact startup error (e.g. `relation "x" does not exist`) to `docs/troubleshooting.md`.
6. Test with H2 (see `backend-test`), plus a throwaway real-Postgres smoke test.
7. Document the table in `database/README.md`, with one or two useful queries.

Open question to raise with the owner: we have no migration tool yet (Flyway would remove the `down -v` step).
