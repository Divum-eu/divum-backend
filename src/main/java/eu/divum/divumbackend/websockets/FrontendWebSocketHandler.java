package eu.divum.divumbackend.websockets;

import eu.divum.divumbackend.dtos.minecraftserverinstance.DaemonConnectionInfo;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.DaemonConnectionException;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceNotFound;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import eu.divum.divumbackend.services.implementations.DaemonConnectionManagerImpl;
import eu.divum.divumbackend.services.implementations.MinecraftInstanceStatusSubscriptionManagerImpl;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RequiredArgsConstructor
@Component
public class FrontendWebSocketHandler extends TextWebSocketHandler {

    @Value("${divum-daemon.ws-scheme}")
    private String daemonWsScheme;

    @Value("${divum-daemon.api-version}/minecraft-servers")
    private String daemonEndpointAddress;

    private final MinecraftInstanceStatusSubscriptionManagerImpl subscriptionManager;
    private final DaemonConnectionManagerImpl daemonConnectionManager;
    private final MinecraftServerInstanceService instanceService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String instanceId = getInstanceIdFromSession(session);
        if (instanceId == null) {
            closeQuietly(session, CloseStatus.BAD_DATA);
            return;
        }

        try {
            DaemonConnectionInfo connectionInfo = instanceService.getDaemonConnectionInfoById(instanceId);
            String daemonUrl = daemonWsScheme + connectionInfo.serverIp() + daemonEndpointAddress + "/" + connectionInfo.daemonId() + "/status/ws";

            session.getAttributes().put("daemonUrl", daemonUrl); // Used when closing the session

            daemonConnectionManager.connectIfNeeded(daemonUrl, instanceId);
            subscriptionManager.addSubscriber(instanceId, daemonUrl, session);
        } catch (Exception e) {
            closeQuietly(session, CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String instanceId = getInstanceIdFromSession(session);
        if (instanceId != null) {
            subscriptionManager.removeSubscriber(instanceId, session);

            String daemonUrl = (String) session.getAttributes().get("daemonUrl");
            if (daemonUrl != null) {
                daemonConnectionManager.disconnectIfEmpty(daemonUrl);
            }
        }
    }

    private String getInstanceIdFromSession(WebSocketSession session) {
        if (session.getUri() == null) return null;

        // example path: /api/v1/minecraft-servers/5b721cc4-6404-455d-9b68-4811437ba977/status
        String path = session.getUri().getPath();
        String[] segments = path.split("/");

        if (segments.length >= 2) {
            return segments[segments.length - 2];
        }

        return null;
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {}
    }
}
