package eu.divum.divumbackend.services;

import eu.divum.divumbackend.dtos.minecraftserverinstance.DaemonConnectionInfo;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import java.util.List;

public interface MinecraftServerInstanceService {

    DaemonConnectionInfo getDaemonConnectionInfoById(String serverId);

    List<MinecraftServerInstanceResponse> getAllByOwner(String userId);

    MinecraftServerInstanceResponse getById(String serverId, String userId);

    MinecraftServerInstanceResponse getByAddress(String address);

    void start(String serverId, String userId);

    void stop(String serverId, String userId);

    void remove(String serverId, String userId);

    String create(MinecraftServerInstanceRequest request, String userId);

    MinecraftServerInstanceResponse update(String serverId, MinecraftServerInstanceRequest request, String userId);
}
