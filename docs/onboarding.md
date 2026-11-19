# Developer onboarding

This guide sets up everything you need to build and run Practice Planning on a
Windows machine. It assumes no prior Linux experience: every command is
explained, and every step ends with a check so you know it worked.

> Status: steps 1–6 were verified on Windows 11 with WSL 3.0.5, Ubuntu 26.04,
> Docker Engine 29.10 and Compose v5.8. Step 7 follows once the app exists.

## What you are setting up, and why

The app is a Kotlin Spring Boot service backed by PostgreSQL. During
development we never install PostgreSQL directly. Instead:

- **Spring Boot Docker Compose** starts the database from `compose.yaml` when
  you run the app from IntelliJ.
- **Testcontainers** starts a throwaway database for each test run.

Both need a *container runtime*: a program that runs Linux containers. On
Windows, containers always run inside a small Linux virtual machine. We use
the plainest version of that:

```
Windows                                  WSL 2 (Linux VM)
┌──────────────────────────────┐         ┌───────────────────────────────┐
│ IntelliJ / Gradle / JVM      │         │ Ubuntu 26.04 LTS              │
│  ├─ Testcontainers ──┐       │         │  └─ dockerd (Docker Engine)   │
│  └─ Spring Boot ─────┤       │  TCP    │      ├─ postgres container    │
│     Docker Compose   ├──────────────────▶      └─ ...                   │
│ docker.exe (CLI only)┘       │ localhost│                              │
└──────────────────────────────┘  :2375  └───────────────────────────────┘
```

- **WSL 2** (Windows Subsystem for Linux) is Microsoft's built-in Linux VM.
- **Ubuntu** is the Linux distribution we install into WSL.
- **Docker Engine** (`dockerd`) is the open-source container daemon, installed
  from Docker's own package repository. No Docker Desktop, no Podman Desktop,
  no GUI, no auto-updater, no account.
- **`docker.exe` and the Compose plugin** are the command-line clients on the
  Windows side. They don't run containers themselves; they talk to `dockerd`.

### Why not Docker Desktop or Podman Desktop?

Both work, but they bundle a GUI, an updater, telemetry and their own WSL
distros, and when they break they are hard to repair. The setup here has
fewer moving parts, every part comes from an official source, and each part
can be inspected and removed on its own.

### The one trade-off you should know about

`dockerd` listens on `tcp://127.0.0.1:2375` **without authentication** so the
Windows JVM can reach it. It is bound to localhost only, so other machines on
your network cannot connect. Any program running *on your own machine* can,
though, and access to Docker is effectively admin access to the Linux VM.
For a personal dev machine that is the common, accepted trade-off.

---

## 1. Check Windows prerequisites

Open **PowerShell** (no admin needed) and run:

```powershell
wsl --version
```

You should see `WSL-Version: 2.x` or newer (or `WSL version: ...`). Run
`wsl --update` from an admin PowerShell to get the latest release. If the command is
unknown, install WSL from an **admin** PowerShell with `wsl --install
--no-distribution` and reboot.

## 2. Install Ubuntu in WSL

```powershell
wsl --install -d Ubuntu-26.04
```

A new window opens and asks for a **UNIX username** and **password**. These
are only for this Linux system and are unrelated to your Windows login.
Pick a short lowercase username (e.g. `alex`) and a password you will
remember: `sudo` (Linux's "run as admin") will ask for it.

When you see a prompt like `alex@MACHINE:~$`, you are inside Linux. Type
`exit` to leave.

**Check:** `wsl -l -v` lists `Ubuntu-26.04` with `VERSION 2`.

### A few Linux basics you'll need

| Command | What it does |
| --- | --- |
| `wsl -d Ubuntu-26.04` | Open a Linux shell from PowerShell |
| `sudo <command>` | Run a command as administrator (asks for your Linux password) |
| `exit` | Leave the Linux shell |
| `systemctl status docker` | Show whether the Docker service is running (`q` to quit) |
| `journalctl -u docker -n 50` | Show the last 50 log lines of the Docker service |

## 3. Install Docker Engine inside Ubuntu

Open the Linux shell (`wsl -d Ubuntu-26.04`) and run the following. This is
the [official Docker install procedure for Ubuntu](https://docs.docker.com/engine/install/ubuntu/),
using Docker's signed apt repository.

```bash
# 3a. Tools needed to add a signed package repository
sudo apt-get update
sudo apt-get install -y ca-certificates curl

# 3b. Docker's package signing key (apt refuses unsigned packages from this repo)
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# 3c. Register the repository for this Ubuntu release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list

# 3d. Install the engine, its runtime (containerd) and the CLI + Compose plugin
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 3e. Let your Linux user talk to Docker without sudo
sudo usermod -aG docker "$USER"
```

Log out and back in (`exit`, then `wsl -d Ubuntu-26.04`) so the group change
applies.

**Check:** `docker run --rm hello-world` prints "Hello from Docker!".

Updates come through the normal Ubuntu update command:
`sudo apt-get update && sudo apt-get upgrade`.

## 4. Make Docker reachable from Windows

By default `dockerd` only listens on a Linux socket file. We add a localhost
TCP listener.

```bash
# 4a. Daemon config: Linux socket + localhost TCP, explicitly without TLS
sudo tee /etc/docker/daemon.json >/dev/null <<'EOF'
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://127.0.0.1:2375"],
  "tls": false
}
EOF

# 4b. The stock service passes "-H fd://", which conflicts with "hosts" above.
#     This override removes it.
sudo mkdir -p /etc/systemd/system/docker.service.d
sudo tee /etc/systemd/system/docker.service.d/override.conf >/dev/null <<'EOF'
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd --containerd=/run/containerd/containerd.sock
EOF

# 4c. Apply
sudo systemctl daemon-reload
sudo systemctl restart docker
```

**Check (in Linux):** `curl -s http://127.0.0.1:2375/version` prints JSON.

> Docker logs a `[DEPRECATION NOTICE]` saying unencrypted TCP access will
> become a hard failure in a future version. It works today. When that
> happens, this step will switch to TLS or an SSH connection.

### 4d. Share `localhost` between Windows and Linux

WSL's default networking forwards Windows `localhost` into the VM through a
relay. Simple requests work, but the relay drops Docker's *streamed*
connections: `docker run` and `docker compose exec` started from Windows
finish successfully yet print nothing. "Mirrored" networking makes Windows
and Linux share one `localhost` directly, which fixes this.

Create the file `%USERPROFILE%\.wslconfig` (in PowerShell):

```powershell
Set-Content "$env:USERPROFILE\.wslconfig" "[wsl2]`r`nnetworkingMode=mirrored`r`n" -Encoding ascii
wsl --shutdown   # restarts WSL so the setting applies; closes open Linux windows
```

If the file already exists, add `networkingMode=mirrored` under its `[wsl2]`
section instead.

**Check (in PowerShell):** `curl.exe -s http://localhost:2375/version` prints
the same JSON as in Linux.

## 5. Keep the Linux VM running

By default WSL stops a distro about 15 seconds after the last Linux window
or `wsl` command ends, and Docker stops with it. IntelliJ then reports that
it can't find Docker. Two settings fix that.

**5a. Don't stop when idle.** Add a `[general]` section to
`%USERPROFILE%\.wslconfig`, so the whole file reads:

```ini
[wsl2]
networkingMode=mirrored

[general]
instanceIdleTimeout=-1
```

Then run `wsl --shutdown` once so it applies.

**5b. Start it when you begin working.** WSL does not start on its own when
Windows boots. Before running the app or tests, start Ubuntu once from
PowerShell. It starts Docker automatically and returns immediately:

```powershell
wsl -d Ubuntu-26.04 --exec true
```

**Check:** wait a minute without any Linux window open, then
`wsl -l -v` still shows `Ubuntu-26.04` as `Running`, and
`curl.exe -s http://localhost:2375/_ping` prints `OK`.

## 6. Install the Docker CLI and Compose on Windows

Spring Boot Docker Compose runs `docker compose ...` on Windows, so we need
the client binaries there. Nothing gets installed system-wide: two files in
your user profile and one `PATH` entry.

1. Download the static Docker CLI from Docker:
   <https://download.docker.com/win/static/stable/x86_64/> (pick the newest
   `docker-<version>.zip`). Extract **only `docker.exe`** to
   `%USERPROFILE%\.docker\bin\`. (The zip also contains `dockerd.exe`, the
   Windows-container daemon; we don't need it.) Note that this binary is not
   code-signed and Docker publishes no checksum for it, so its integrity
   rests on the HTTPS download from `download.docker.com`. Use the same
   version as the engine (`docker version` in Linux shows it).
2. Download `docker-compose-windows-x86_64.exe` from the
   [latest Compose release](https://github.com/docker/compose/releases/latest),
   verify it against the release's `checksums.txt`, and save it as
   `%USERPROFILE%\.docker\cli-plugins\docker-compose.exe`.
3. In PowerShell, add the CLI to your user `PATH` and point it at WSL:

   ```powershell
   $bin = "$env:USERPROFILE\.docker\bin"
   [Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path','User') + ";$bin", 'User')
   [Environment]::SetEnvironmentVariable('DOCKER_HOST', 'tcp://localhost:2375', 'User')
   ```

   Close and reopen PowerShell **and IntelliJ** so they pick up the new
   variables.

`DOCKER_HOST` is read by `docker.exe`, by Compose and by Testcontainers, so
this one variable connects all three.

**Check:**

```powershell
docker version          # shows both Client and Server sections
docker compose version
docker run --rm hello-world   # must print "Hello from Docker!", not nothing
```

## 7. Verify with the project (once the app exists)

Once the app skeleton exists: run the tests from IntelliJ or with
`./gradlew test`. Testcontainers should start PostgreSQL 18 and the tests
should pass.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `Cannot connect to the Docker daemon at tcp://localhost:2375` | Ubuntu isn't running (e.g. after a reboot) or Docker is stopped | `wsl -d Ubuntu-26.04 --exec true`, then if it persists `wsl -d Ubuntu-26.04 -- systemctl status docker` |
| Testcontainers: `Could not find a valid Docker environment` | IntelliJ was started before `DOCKER_HOST` was set | Restart IntelliJ completely |
| `wsl --install` fails with `...MountDisk/HCS/ERROR_FILE_NOT_FOUND` mentioning `C:\Program Files\WSL\system.vhd` | WSL's own installation is damaged (a file is missing, e.g. after a manual cleanup) | In an admin PowerShell: `wsl --update`, then `wsl --shutdown`, then retry |
| `wsl --update` fails with `The older version of Windows Subsystem for Linux cannot be removed` (exit code 1603) | Windows' saved copy of the installed WSL installer (in `C:\Windows\Installer`) is corrupt, so the old version can't be uninstalled | Download the installer for *your current* version (`wsl --version`) from [WSL releases](https://github.com/microsoft/WSL/releases), check it is signed by Microsoft (file Properties → Digital Signatures), rename it to `wsl.msi`, then in an admin shell run `msiexec /fomusv "<path>\wsl.msi"`. This repairs and re-saves it. Then `wsl --update` works |
| `docker run` from Windows succeeds but prints nothing | WSL's default localhost relay drops streamed connections | Do step 4d (mirrored networking) |
| `docker` works in Linux but not in Windows | TCP listener not configured | Redo step 4, check `journalctl -u docker -n 50` |

## Removing everything

```powershell
wsl --unregister Ubuntu-26.04   # deletes the Linux VM and ALL data in it
Remove-Item -Recurse "$env:USERPROFILE\.docker"
[Environment]::SetEnvironmentVariable('DOCKER_HOST', $null, 'User')
```

Then remove `%USERPROFILE%\.docker\bin` from your user `PATH`.
