package eu.divum.divumbackend.services.interfaces;

import java.util.concurrent.CompletableFuture;

public interface ServerInstanceService {
    public CompletableFuture<Void> startServer(String serverId);

}
