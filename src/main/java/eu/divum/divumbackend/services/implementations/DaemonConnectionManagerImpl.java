package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.exceptions.minecraftserverinstance.DaemonConnectionException;
import eu.divum.divumbackend.services.DaemonConnectionManager;
import eu.divum.divumbackend.websockets.DaemonWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class DaemonConnectionManagerImpl implements DaemonConnectionManager {

    private final ConcurrentHashMap<String, WebSocketSession> activeDaemons = new ConcurrentHashMap<>();
    private final MinecraftInstanceStatusSubscriptionManagerImpl subscriptionManager;

    @Override
    public void connectIfNeeded(String daemonUrl, String instanceId) {
        // Creates a new connection to the daemon, if one doesn't exist
        activeDaemons.computeIfAbsent(daemonUrl, url -> {
            StandardWebSocketClient client = new StandardWebSocketClient();
            try {
                DaemonWebSocketHandler handler = new DaemonWebSocketHandler(
                        subscriptionManager, this, url, instanceId
                );

                return client.execute(handler, url).get();
            } catch (Exception e) {
                throw new DaemonConnectionException();
            }
        });
    }

    @Override
    public void disconnectIfEmpty(String daemonUrl) {
        if (!subscriptionManager.hasSubscribersForDaemon(daemonUrl)) {
            WebSocketSession session = activeDaemons.remove(daemonUrl);
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void removeDeadConnection(String daemonUrl) {
        activeDaemons.remove(daemonUrl);
    }
}
