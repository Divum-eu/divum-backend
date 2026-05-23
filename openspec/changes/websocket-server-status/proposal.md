## Why

The platform currently has no way to show users the live state of their Minecraft server instances. To build a real-time dashboard on the web client, the backend needs a channel for streaming server metrics from the daemon. WebSockets provide a persistent, low-latency connection suited to this.

## What Changes

- Add a WebSocket endpoint to `divum-daemon` (FastAPI) that accepts connections from the backend and streams container status on demand.
- Add WebSocket support to `divum-backend` (Spring Boot) — both a client side (connecting to daemons) and a server side (serving browser clients).
- The backend lazily connects to a daemon when a browser client first requests to watch a server on that machine.
- The daemon polls Docker container stats and RCON for watched servers at a configurable interval (default 3s) and pushes updates over the WebSocket.
- The backend forwards daemon responses to connected browser clients as-is.
- No authentication is added for now.

## Capabilities

### New Capabilities
- `daemon-ws-status`: WebSocket endpoint on the daemon that accepts watch/unwatch commands from the backend and streams container metrics (status, CPU%, memory, player count).
- `backend-ws-relay`: WebSocket infrastructure on the backend that connects to daemons, manages client subscriptions, and relays status updates to browser clients.

### Modified Capabilities
<!-- None — this is a new feature with no changes to existing spec-level behavior. -->

## Impact

- **divum-daemon**: New FastAPI WebSocket route. New service for collecting Docker stats and RCON player count. Existing `docker_server_manager.py` and `_run_rcon` are reused but not modified.
- **divum-backend**: New Maven dependency (`spring-boot-starter-websocket`). New WebSocket config, handlers, and session manager classes. New `.env` / `application.properties` entries for daemon WS URLs.
- **divum-web-client**: Not changed in this proposal, but this feature is designed to be consumed by a future dashboard page.
- **APIs**: Two new WebSocket endpoints (daemon-side and backend-side). No existing REST APIs are modified.
