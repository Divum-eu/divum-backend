## ADDED Requirements

### Requirement: Daemon accepts WebSocket connections for status streaming
The daemon SHALL expose a WebSocket endpoint at `/ws/status` that accepts connections from the backend.

#### Scenario: Backend connects successfully
- **WHEN** the backend opens a WebSocket connection to `ws://<daemon-ip>:<daemon-port>/ws/status`
- **THEN** the daemon SHALL accept the connection and keep it open

### Requirement: Daemon processes watch commands
The daemon SHALL accept JSON messages with `{"type": "watch", "server_id": "<daemon_id>"}` and begin polling status for the specified container.

#### Scenario: Watch a running server
- **WHEN** the daemon receives `{"type": "watch", "server_id": "abc-123"}`
- **THEN** the daemon SHALL start polling Docker stats and RCON for container `abc-123` at the configured interval

#### Scenario: Watch a non-existent server
- **WHEN** the daemon receives a watch command for a container ID that does not exist
- **THEN** the daemon SHALL send a status update with `"status": "unreachable"` and zeroed metrics

### Requirement: Daemon processes unwatch commands
The daemon SHALL accept JSON messages with `{"type": "unwatch", "server_id": "<daemon_id>"}` and stop polling for that container.

#### Scenario: Unwatch a watched server
- **WHEN** the daemon receives `{"type": "unwatch", "server_id": "abc-123"}`
- **THEN** the daemon SHALL stop polling stats for container `abc-123`

#### Scenario: Unwatch a server that is not being watched
- **WHEN** the daemon receives an unwatch command for a server it is not currently watching
- **THEN** the daemon SHALL ignore the command without error

### Requirement: Daemon sends status updates at a configurable interval
The daemon SHALL send a JSON status update for each watched server at a regular interval, configurable via `STATUS_POLL_INTERVAL_SECONDS` (default: 3).

#### Scenario: Periodic status update for a running server
- **WHEN** a server is being watched and the poll interval elapses
- **THEN** the daemon SHALL send a JSON message containing `server_id`, `status`, `cpu_percent`, `memory_usage_mb`, `memory_limit_mb`, `player_count`, and `player_max`

### Requirement: Daemon collects CPU and memory from Docker stats
The daemon SHALL compute CPU percentage from a single `container.stats(stream=False)` call using `cpu_stats` and `precpu_stats` deltas. Memory SHALL be calculated as working-set usage (usage minus cache) divided by limit, converted to megabytes.

#### Scenario: Stats collected for a running container
- **WHEN** the daemon polls a running container
- **THEN** `cpu_percent` SHALL be a float representing percentage of allocated CPU, and `memory_usage_mb` and `memory_limit_mb` SHALL be integers in megabytes

### Requirement: Daemon collects player count from RCON
The daemon SHALL execute `rcon-cli list` via `container.exec_run` and parse the output to extract current player count and max players.

#### Scenario: RCON available
- **WHEN** the daemon executes `rcon-cli list` and receives a response
- **THEN** `player_count` and `player_max` SHALL be populated from the parsed response

#### Scenario: RCON unavailable
- **WHEN** the `rcon-cli list` command fails or times out
- **THEN** `player_count` and `player_max` SHALL be `null` in the status update

### Requirement: Daemon reports unreachable containers
The daemon SHALL send `"status": "unreachable"` with zeroed/null metrics when a watched container cannot be reached.

#### Scenario: Container was deleted while being watched
- **WHEN** a watched container no longer exists in Docker
- **THEN** the daemon SHALL send a status update with `"status": "unreachable"`, `cpu_percent: 0`, `memory_usage_mb: 0`, `memory_limit_mb: 0`, `player_count: null`, `player_max: null`
