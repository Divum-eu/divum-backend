## 1. Daemon — WebSocket endpoint and status collector

- [x] 1.1 Create `src/services/minecraft/status_collector.py` — service that takes a container ID, calls `container.stats(stream=False)` via `asyncio.to_thread`, computes CPU% from `cpu_stats`/`precpu_stats` deltas and memory from `memory_stats`, executes `rcon-cli list` to parse player count/max, and returns a status dict
- [x] 1.2 Create `src/schemas/server_status_update.py` — Pydantic model for the outbound status JSON (`server_id`, `status`, `cpu_percent`, `memory_usage_mb`, `memory_limit_mb`, `player_count`, `player_max`)
- [x] 1.3 Create `src/schemas/watch_command.py` — Pydantic model for inbound watch/unwatch commands (`type`, `server_id`)
- [x] 1.4 Create `src/routers/ws_status_router.py` — FastAPI WebSocket route at `/ws/status` that accepts a connection, listens for watch/unwatch JSON messages, maintains a set of watched server IDs, and runs a polling loop per watched server at the configured interval using `status_collector`
- [x] 1.5 Add `STATUS_POLL_INTERVAL_SECONDS` to `.env-example` (default `3`)
- [x] 1.6 Register the new WebSocket router in `main.py`

## 2. Backend — WebSocket server for browser clients

- [x] 2.1 Add `spring-boot-starter-websocket` dependency to `pom.xml`
- [x] 2.2 Create `WebSocketConfig.java` — register the client-facing WebSocket handler at `/v1/minecraft-servers/*/status` and set allowed origins
- [x] 2.3 Create `ClientStatusWebSocketHandler.java` — on connect, extract the server instance ID from the URI path, look up `daemonId` and `ServerMachine.ip` from the repository, register the session, and trigger a watch on the daemon connection. On close, deregister and send unwatch if last client for that server.

## 3. Backend — WebSocket client to daemon

- [x] 3.1 Create `DaemonWebSocketManager.java` — manages a map of `machineIp → WebSocketSession`. Provides `watch(machineIp, daemonPort, daemonId)` and `unwatch(machineIp, daemonId)` methods. Lazily connects to `ws://<ip>:<port>/ws/status` on first watch. Closes connection on last unwatch for a machine.
- [x] 3.2 Handle incoming daemon messages in `DaemonWebSocketManager` — parse the status JSON from the daemon, look up which browser client sessions are watching that `server_id`, and forward the message
- [x] 3.3 Handle daemon disconnection — on connection lost, send `{"server_id": "...", "status": "unavailable"}` to all browser clients watching servers on that machine. Implement reconnection with backoff.

## 4. Backend — Repository and configuration

- [x] 4.1 Add a query to `MinecraftServerInstanceRepository` to fetch `daemonId` and `serverMachine.ip` by server instance ID
- [x] 4.2 Add `divum-daemon.ws-port` property to `application.properties` / `.env` (default `8000`)

## 5. Verification

- [x] 5.1 Start daemon locally, connect via Bruno WebSocket to the backend's `/v1/minecraft-servers/{id}/status` endpoint, verify status updates arrive every 3 seconds with valid CPU, RAM, status, and player data
- [x] 5.2 Verify unwatch is sent when the last browser client disconnects (check daemon logs)
- [x] 5.3 Verify daemon disconnection sends `"status": "unavailable"` to the browser client
