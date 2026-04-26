package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface MinecraftServerInstanceService {

    MinecraftServerInstanceResponse getById(String serverId);

    MinecraftServerInstanceResponse getByAddress(String address);

    List<MinecraftServerInstanceResponse> getForUser(String serverId, String ownerId);

    List<MinecraftServerInstanceResponse> getAllForUser(String ownerId);

    void start(String serverId);

    void stop(String serverId);

    void delete(String serverId);

    UUID create(MinecraftServerInstanceRequest request);
}
