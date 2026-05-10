package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

public interface MinecraftServerInstanceService {

    MinecraftServerInstanceResponse getById(String serverId);

    MinecraftServerInstanceResponse getByAddress(String address);

    void start(String serverId);

    void stop(String serverId);

    void remove(String serverId);

    String create(MinecraftServerInstanceRequest request);
}
