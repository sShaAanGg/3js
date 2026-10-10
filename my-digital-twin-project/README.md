# Digital Twin (Spring Boot + Angular/Three.js + PostgreSQL)

```
my-digital-twin-project/
├── backend/            Spring Boot 3 (Java 21, Maven): REST API + sensor simulator
├── frontend/           Angular 19 + Three.js, served by nginx (proxies /api → backend)
├── database/init.sql   Schema + seed assets (runs on first DB start)
└── docker-compose.yml  db + backend + frontend
```

How the pieces fit together:

- **db** (`postgres:16`) holds the `asset` table. `init.sql` seeds four assets.
- **backend** exposes `GET /api/assets`, `GET /api/assets/{id}` and `PUT /api/assets/{id}/position`.
  `SensorSimulator` changes each asset's temperature every 2 s and sets its status to OK, WARNING (≥35 °C) or ALARM (≥45 °C).
- **frontend** polls `/api/assets` every 2 s and draws each asset as a mesh.
  WARNING assets glow orange and ALARM assets pulse red. Use the mouse to orbit, pan and zoom.

## 1. Install Docker CE (Ubuntu / WSL2)

These steps install Docker Engine inside the WSL Ubuntu distro, without Docker Desktop.

**WSL only:** first make sure systemd is on. Check that `/etc/wsl.conf` contains the lines below:

```ini
[boot]
systemd=true
```

If you had to add them, run `wsl --shutdown` from Windows PowerShell and then reopen Ubuntu.

```bash
# Remove any old or unofficial packages (safe if none are installed)
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do
  sudo apt-get remove -y $pkg
done

# Add Docker's official GPG key and apt repository
sudo apt-get update
sudo apt-get install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine, CLI, containerd, Buildx and the Compose plugin
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Start Docker now and on every boot
sudo systemctl enable --now docker

# Let your user run docker without sudo (then log out/in, or run `newgrp docker`)
sudo usermod -aG docker $USER

# Verify
docker run --rm hello-world
docker compose version
```

> If `apt-get update` reports that Docker's repo has no `Release` file for your Ubuntu codename,
> Docker hasn't published packages for that release yet. In the `docker.list` line, replace the
> codename with the newest LTS that Docker lists at https://download.docker.com/linux/ubuntu/dists/,
> then run the install again.

## 2. Run the stack

```bash
cd my-digital-twin-project
cp .env.example .env          # optional; edit credentials here
docker compose up --build     # add -d to run in the background
```

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

## 3. Local development without rebuilding images (optional)

```bash
docker compose up db                            # just the database
cd backend  && mvn spring-boot:run              # needs JDK 21 + Maven
cd frontend && npm install && npm start         # http://localhost:4200, /api proxied to :8080
```
