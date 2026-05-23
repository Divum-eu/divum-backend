## Context

The Divum platform manages Minecraft server instances running as Docker containers on remote machines via `divum-daemon` (Python/FastAPI). The `divum-backend` (Java/Spring Boot) orchestrates daemon calls over REST. There is currently no mechanism for streaming live server metrics to the frontend.

The daemon already has:
- Docker SDK access for container lifecycle and stats (`docker_server_manager.py`)
- RCON command execution via `container.exec_run("rcon-cli ...")` (`_run_rcon`)
- A `MinecraftServerStatus` schema with `status` and `log` fields

The backend already has:
- `ServerMachine` entity with an `ip` field
- `MinecraftServerInstance` entity with a `daemonId` referencing the Docker container name
- REST-based communication with daemons using `HttpClient`

## Goals / Non-Goals

**Goals:**
- Stream live server metrics (status, CPU, RAM, player count) from daemon to backend to browser client via WebSockets.
- Only poll metrics for servers actively watched by at least one browser client.
- Support multiple daemons (one per server machine), with lazy connection establishment.

**Non-Goals:**
- Authentication/authorization on the WebSocket endpoints (deferred).
- Modifying existing REST APIs or the `MinecraftServerStatus` schema.
- Frontend dashboard implementation (separate future change).
- Historical metrics storage or aggregation.

## Decisions

### 1. Backend initiates WS connection to daemon (not the reverse)

The backend knows which `ServerMachine` hosts each server instance. When a browser client wants to watch a server, the backend looks up the machine IP and connects to the daemon's WS endpoint.

**Alternatives considered:**
- *Daemon connects to backend*: Requires the daemon to know the backend's address and maintain a persistent connection. Less natural since the backend is the orchestrator.
- *Polling REST endpoint*: Simpler but higher latency and overhead for real-time updates.

**Rationale:** Keeps the daemon stateless with respect to the backend's address. The backend already has all the routing information it needs.

### 2. Lazy connection lifecycle

The backend establishes a WebSocket connection to a daemon only when the first browser client requests to watch a server on that machine. The connection is closed when no more servers on that machine are being watched.

**Alternatives considered:**
- *Eager connection on startup*: Wastes resources connecting to machines with no active watchers.

**Rationale:** Minimizes idle connections. The small delay on first watch is acceptable.

### 3. Watch/unwatch protocol over a single WS connection per daemon

A single WebSocket connection between backend and daemon carries watch/unwatch commands for all servers on that machine. The daemon maintains a set of watched server IDs and runs a polling loop for each.

**Message format (snake_case JSON):**

```
Backend → Daemon:
  {"type": "watch",   "server_id": "<daemon_id>"}
  {"type": "unwatch", "server_id": "<daemon_id>"}

Daemon → Backend:
  {
    "server_id": "<daemon_id>",
    "status": "running" | "exited" | "created" | "paused" | "dead" | "restarting" | "unreachable",
    "cpu_percent": 12.4,
    "memory_usage_mb": 1024,
    "memory_limit_mb": 4096,
    "player_count": 3,
    "player_max": 20
  }
```

### 4. CPU/RAM from Docker stats, player count from RCON

- **CPU**: Single `container.stats(stream=False)` call. Compute percentage from `cpu_stats` and `precpu_stats` delta values.
- **RAM**: `memory_stats.usage` (minus cache) divided by `memory_stats.limit`.
- **Player count**: `container.exec_run("rcon-cli list")`, parse "X of a max of Y players online".
- **Fallback**: If RCON fails or container is unreachable, send `"status": "unreachable"` with zeroed metrics.

### 5. Configurable poll interval, default 3 seconds

The daemon's polling interval is configurable via environment variable `STATUS_POLL_INTERVAL_SECONDS`, defaulting to `3`.

### 6. Spring Boot WebSocket for the backend

Add `spring-boot-starter-websocket` dependency. Use raw `WebSocketHandler` (not STOMP/SockJS) to keep it simple and aligned with the daemon's raw WebSocket.

- Client endpoint: `/v1/minecraft-servers/{id}/status` (browser connects here)
- Daemon endpoint: internal, managed by the backend's `WebSocketClient` connecting to `ws://<daemon-ip>:<daemon-port>/ws/status`

## Risks / Trade-offs

- **Single WS connection per daemon is a single point of failure** → Implement reconnection with backoff on the backend side. Send `"status": "unavailable"` to all browser clients watching servers on that machine when the connection drops.
- **RCON may not be available on all containers** → If `exec_run` fails, report `player_count: null` rather than crashing the poll loop.
- **Docker `stats()` call is blocking** → Already mitigated by running in `asyncio.to_thread` as done elsewhere in the daemon codebase.
- **No auth on WS endpoints** → Acceptable for now; tracked as a future change.
