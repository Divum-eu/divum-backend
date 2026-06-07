package eu.divum.divumbackend.services;

import org.springframework.web.socket.WebSocketSession;

public interface MinecraftInstanceStatusSubscriptionManager {

    void addSubscriber(String instanceId, String daemonUrl, WebSocketSession session);

    void removeSubscriber(String instanceId, WebSocketSession session);

    boolean hasSubscribersForDaemon(String daemonUrl);

    void broadcastToSubscribers(String instanceId, String payload);
}
