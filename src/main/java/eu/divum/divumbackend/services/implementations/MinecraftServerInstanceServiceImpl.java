package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import eu.divum.divumbackend.services.MinecraftServerInstanceService;

import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MinecraftServerInstanceServiceImpl implements MinecraftServerInstanceService {
    private final MinecraftServerInstanceRepository repository;


    @Override
    public MinecraftServerInstanceResponse getById(String serverId) {
        return null;
    }

    @Override
    public MinecraftServerInstanceResponse getByAddress(String address) {
        return null;
    }

    @Override
    public List<MinecraftServerInstanceResponse> getForUser(String serverId, String ownerId) {
        return List.of();
    }

    @Override
    public List<MinecraftServerInstanceResponse> getAllForUser(String ownerId) {
        return List.of();
    }

    @Override
    public void start(String serverId) {

    }

    @Override
    public void stop(String serverId) {

    }

    @Override
    public void delete(String serverId) {

    }

    @Override
    public UUID create(MinecraftServerInstanceRequest request) {
        return null;
    }
}
