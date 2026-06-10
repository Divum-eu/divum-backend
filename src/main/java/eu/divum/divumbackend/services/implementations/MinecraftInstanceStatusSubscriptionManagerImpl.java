package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.services.MinecraftInstanceStatusSubscriptionManager;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class MinecraftInstanceStatusSubscriptionManagerImpl implements MinecraftInstanceStatusSubscriptionManager {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> instanceDaemonMap = new ConcurrentHashMap<>();

    @Override
    public void addSubscriber(String instanceId, String daemonUrl, WebSocketSession session) {
        instanceDaemonMap.put(instanceId, daemonUrl);
        subscriptions.computeIfAbsent(instanceId, _ -> new CopyOnWriteArraySet<>()).add(session);
    }

    @Override
    public void removeSubscriber(String instanceId, WebSocketSession session) {
        Set<WebSocketSession> sessions = subscriptions.get(instanceId);
        if (sessions != null && session != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                subscriptions.remove(instanceId);
                instanceDaemonMap.remove(instanceId);
            }
        }
    }

    @Override
    public boolean hasSubscribersForDaemon(String daemonUrl) {
        return instanceDaemonMap.containsValue(daemonUrl);
    }

    @Override
    public void broadcastToSubscribers(String instanceId, String payload) {
        Set<WebSocketSession> sessions = subscriptions.get(instanceId);
        if (sessions != null) {
            TextMessage message = new TextMessage(payload);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        removeSubscriber(instanceId, session);
                    }
                }
            }
        }
    }
}
