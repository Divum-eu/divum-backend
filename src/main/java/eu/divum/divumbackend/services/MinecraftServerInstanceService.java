package eu.divum.divumbackend.services;

import eu.divum.divumbackend.services.interfaces.ServerInstanceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MinecraftServerInstanceService implements ServerInstanceService {
    @Async
    @Override
    public CompletableFuture<Void> startServer(String serverId) {
        return null;
    }
}
