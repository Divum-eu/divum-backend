package eu.divum.divumbackend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientStatusWebSocketHandler extends TextWebSocketHandler {

    private final DaemonWebSocketManager daemonWebSocketManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length < 4) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        try {
            UUID serverId = UUID.fromString(parts[3]);
            daemonWebSocketManager.registerClient(session, serverId);
        } catch (IllegalArgumentException e) {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        daemonWebSocketManager.removeClient(session);
    }
}
