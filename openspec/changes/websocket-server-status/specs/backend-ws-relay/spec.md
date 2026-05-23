## ADDED Requirements

### Requirement: Backend exposes a WebSocket endpoint for browser clients
The backend SHALL expose a WebSocket endpoint at `/v1/minecraft-servers/{id}/status` where `{id}` is the server instance UUID.

#### Scenario: Browser client connects to watch a server
- **WHEN** a browser client opens a WebSocket connection to `/v1/minecraft-servers/abc-123/status`
- **THEN** the backend SHALL accept the connection and begin relaying status updates for that server

### Requirement: Backend connects to the daemon lazily
The backend SHALL establish a WebSocket connection to the daemon hosting a server only when the first browser client requests to watch a server on that machine. The connection SHALL be reused for subsequent watchers on the same machine.

#### Scenario: First watcher triggers daemon connection
- **WHEN** a browser client watches a server and no WebSocket connection exists to that server's daemon
- **THEN** the backend SHALL look up the `ServerMachine.ip` for the server, connect to `ws://<ip>:<daemon-port>/ws/status`, and send a watch command

#### Scenario: Subsequent watcher reuses existing connection
- **WHEN** a browser client watches a server on a machine that already has an active daemon WebSocket connection
- **THEN** the backend SHALL reuse the existing connection and send an additional watch command

### Requirement: Backend sends watch/unwatch commands to daemon
The backend SHALL send `{"type": "watch", "server_id": "<daemon_id>"}` when a browser client starts watching, and `{"type": "unwatch", "server_id": "<daemon_id>"}` when the last browser client for that server disconnects.

#### Scenario: Last client disconnects from a server
- **WHEN** the last browser client watching server `abc-123` disconnects
- **THEN** the backend SHALL send `{"type": "unwatch", "server_id": "<daemon_id>"}` to the daemon

#### Scenario: Last client on a machine disconnects
- **WHEN** the last browser client watching any server on a given machine disconnects
- **THEN** the backend SHALL send the unwatch command and close the WebSocket connection to that daemon

### Requirement: Backend forwards daemon status updates to browser clients
The backend SHALL forward status update messages from the daemon to all browser clients watching the corresponding server, without transformation.

#### Scenario: Status update received from daemon
- **WHEN** the backend receives a status update for `server_id: "abc-123"` from the daemon
- **THEN** the backend SHALL forward the JSON message to all browser WebSocket sessions watching the server instance that has `daemon_id = "abc-123"`

### Requirement: Backend handles daemon disconnection
The backend SHALL detect when a daemon WebSocket connection drops and notify all affected browser clients.

#### Scenario: Daemon connection lost
- **WHEN** the WebSocket connection to a daemon is lost
- **THEN** the backend SHALL send `{"server_id": "<daemon_id>", "status": "unavailable"}` to all browser clients watching servers on that machine
