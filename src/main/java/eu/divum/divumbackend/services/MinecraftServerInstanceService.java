package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import java.util.List;

public interface MinecraftServerInstanceService {

    MinecraftServerInstanceResponse getById(String serverId);

    MinecraftServerInstanceResponse getByAddress(String address);

    List<MinecraftServerInstanceResponse> getAllForOwner(String ownerId);

    void start(String serverId);

    void stop(String serverId);

    void remove(String serverId);

    String create(MinecraftServerInstanceRequest request);
}
