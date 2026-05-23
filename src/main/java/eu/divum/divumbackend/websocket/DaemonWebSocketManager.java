package eu.divum.divumbackend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;
import eu.divum.divumbackend.domain.MinecraftServerInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DaemonWebSocketManager {

    private final MinecraftServerInstanceRepository repository;
    private final ObjectMapper objectMapper;
    private final WebSocketClient webSocketClient;
    
    @Value("${divum-daemon.ws-port:8000}")
    private int daemonWsPort;

    // machineIp -> daemon WebSocket session
    private final Map<String, WebSocketSession> daemonSessions = new ConcurrentHashMap<>();

    // browser client sessionId -> serverId
    private final Map<String, UUID> clientSessionsToServer = new ConcurrentHashMap<>();

    // serverId -> Set of browser client sessionIds
    private final Map<UUID, Set<String>> serverToClients = new ConcurrentHashMap<>();

    // serverId -> browser client WebSocket session
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();

    // daemonId -> serverId
    private final Map<String, UUID> daemonToServer = new ConcurrentHashMap<>();

    public DaemonWebSocketManager(MinecraftServerInstanceRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.webSocketClient = new StandardWebSocketClient();
    }

    public synchronized void registerClient(WebSocketSession clientSession, UUID serverId) {
        String clientId = clientSession.getId();
        clientSessions.put(clientId, clientSession);
        clientSessionsToServer.put(clientId, serverId);
        
        Set<String> watchers = serverToClients.computeIfAbsent(serverId, k -> ConcurrentHashMap.newKeySet());
        boolean isFirstWatcher = watchers.isEmpty();
        watchers.add(clientId);

        if (isFirstWatcher) {
            triggerWatch(serverId);
        }
    }

    public synchronized void removeClient(WebSocketSession clientSession) {
        String clientId = clientSession.getId();
        clientSessions.remove(clientId);
        UUID serverId = clientSessionsToServer.remove(clientId);
        if (serverId != null) {
            Set<String> watchers = serverToClients.get(serverId);
            if (watchers != null) {
                watchers.remove(clientId);
                if (watchers.isEmpty()) {
                    triggerUnwatch(serverId);
                }
            }
        }
    }

    private void triggerWatch(UUID serverId) {
        repository.findById(serverId).ifPresent(instance -> {
            String daemonId = instance.getDaemonId();
            String machineIp = instance.getServerMachine().getIp();
            
            daemonToServer.put(daemonId, serverId);

            WebSocketSession session = getOrCreateDaemonSession(machineIp);
            if (session != null && session.isOpen()) {
                sendWatchCommand(session, "watch", daemonId);
            }
        });
    }

    private void triggerUnwatch(UUID serverId) {
        repository.findById(serverId).ifPresent(instance -> {
            String daemonId = instance.getDaemonId();
            String machineIp = instance.getServerMachine().getIp();

            WebSocketSession session = daemonSessions.get(machineIp);
            if (session != null && session.isOpen()) {
                sendWatchCommand(session, "unwatch", daemonId);
                
                // Check if there are any other watched servers on this machine
                boolean hasOtherWatchers = false;
                for (UUID sId : serverToClients.keySet()) {
                    if (!serverToClients.get(sId).isEmpty()) {
                        Optional<MinecraftServerInstance> otherInstance = repository.findById(sId);
                        if (otherInstance.isPresent() && otherInstance.get().getServerMachine().getIp().equals(machineIp)) {
                            hasOtherWatchers = true;
                            break;
                        }
                    }
                }
                if (!hasOtherWatchers) {
                    try {
                        session.close();
                        daemonSessions.remove(machineIp);
                    } catch (IOException e) {
                        log.error("Error closing daemon session", e);
                    }
                }
            }
        });
    }

    private WebSocketSession getOrCreateDaemonSession(String machineIp) {
        WebSocketSession session = daemonSessions.get(machineIp);
        if (session != null && session.isOpen()) {
            return session;
        }

        try {
            String uri = "ws://" + machineIp + ":" + daemonWsPort + "/ws/status";
            WebSocketSession newSession = webSocketClient.execute(new DaemonWebSocketHandler(machineIp), uri).get();
            daemonSessions.put(machineIp, newSession);
            
            // Re-send watches for this machine
            for (UUID sId : serverToClients.keySet()) {
                if (!serverToClients.get(sId).isEmpty()) {
                    repository.findById(sId).ifPresent(instance -> {
                        if (instance.getServerMachine().getIp().equals(machineIp)) {
                            sendWatchCommand(newSession, "watch", instance.getDaemonId());
                        }
                    });
                }
            }
            
            return newSession;
        } catch (Exception e) {
            log.error("Failed to connect to daemon at {}", machineIp, e);
            notifyClientsOfUnavailableDaemon(machineIp);
            return null;
        }
    }

    private void sendWatchCommand(WebSocketSession session, String type, String daemonId) {
        try {
            Map<String, String> command = Map.of("type", type, "server_id", daemonId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(command)));
        } catch (IOException e) {
            log.error("Error sending command to daemon", e);
        }
    }

    private void notifyClientsOfUnavailableDaemon(String machineIp) {
        try {
            for (UUID serverId : serverToClients.keySet()) {
                repository.findById(serverId).ifPresent(instance -> {
                    if (instance.getServerMachine().getIp().equals(machineIp)) {
                        String daemonId = instance.getDaemonId();
                        try {
                            Map<String, Object> offlineStatus = new HashMap<>();
                            offlineStatus.put("server_id", daemonId);
                            offlineStatus.put("status", "unavailable");
                            String message = objectMapper.writeValueAsString(offlineStatus);
                            broadcastToClients(serverId, message);
                        } catch (JsonProcessingException e) {
                            log.error("Error creating offline message", e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error creating offline message", e);
        }
    }

    private void broadcastToClients(UUID serverId, String message) {
        Set<String> watchers = serverToClients.get(serverId);
        if (watchers != null) {
            for (String clientId : watchers) {
                WebSocketSession clientSession = clientSessions.get(clientId);
                if (clientSession != null && clientSession.isOpen()) {
                    try {
                        clientSession.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("Error sending to client", e);
                    }
                }
            }
        }
    }

    private class DaemonWebSocketHandler extends TextWebSocketHandler {
        private final String machineIp;

        public DaemonWebSocketHandler(String machineIp) {
            this.machineIp = machineIp;
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String daemonId = (String) data.get("server_id");
            if (daemonId != null) {
                UUID serverId = daemonToServer.get(daemonId);
                if (serverId != null) {
                    broadcastToClients(serverId, payload);
                }
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            daemonSessions.remove(machineIp);
            notifyClientsOfUnavailableDaemon(machineIp);
        }
    }
}
