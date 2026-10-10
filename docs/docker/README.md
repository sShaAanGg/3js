# Installing Docker CE

Setup guide for running the digital twin project's containers. These steps install Docker Engine inside the WSL Ubuntu distro, without Docker Desktop.

After installing, go back to the [project README](../../README.md) to run the stack.

## 1. Enable systemd (WSL only)

Check that `/etc/wsl.conf` contains the lines below:

```ini
[boot]
systemd=true
```

If you had to add them, run `wsl --shutdown` from Windows PowerShell and then reopen Ubuntu.

## 2. Install Docker Engine

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
