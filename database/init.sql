CREATE TABLE IF NOT EXISTS asset (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)     NOT NULL,
    shape       VARCHAR(20)      NOT NULL,                -- BOX | SPHERE | CYLINDER
    pos_x       DOUBLE PRECISION NOT NULL DEFAULT 0,
    pos_y       DOUBLE PRECISION NOT NULL DEFAULT 0,
    pos_z       DOUBLE PRECISION NOT NULL DEFAULT 0,
    color       VARCHAR(7)       NOT NULL DEFAULT '#3366ff',
    status      VARCHAR(20)      NOT NULL DEFAULT 'OK',   -- OK | WARNING | ALARM
    temperature DOUBLE PRECISION NOT NULL DEFAULT 20,
    updated_at  TIMESTAMPTZ      NOT NULL DEFAULT now()
);

INSERT INTO asset (name, shape, pos_x, pos_y, pos_z, color, temperature) VALUES
    ('Pump A',     'BOX',       0, 0.5, 0, '#0000ff', 22.0),
    ('Tank B',     'SPHERE',    2, 0.5, 0, '#00ff00', 18.5),
    ('Motor C',    'CYLINDER', -2, 0.5, 1, '#ff8800', 30.0),
    ('Conveyor D', 'BOX',       0, 0.5, 3, '#aa00ff', 25.0);
