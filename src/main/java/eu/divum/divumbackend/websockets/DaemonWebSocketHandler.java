package eu.divum.divumbackend.websockets;

import eu.divum.divumbackend.services.implementations.DaemonConnectionManagerImpl;
import eu.divum.divumbackend.services.implementations.MinecraftInstanceStatusSubscriptionManagerImpl;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@AllArgsConstructor
public class DaemonWebSocketHandler extends TextWebSocketHandler {

    private final MinecraftInstanceStatusSubscriptionManagerImpl subscriptionManager;
    private final DaemonConnectionManagerImpl connectionManager;
    private final String daemonUrl;
    private final String instanceId;

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        subscriptionManager.broadcastToSubscribers(instanceId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        connectionManager.removeDeadConnection(daemonUrl);
    }
}
