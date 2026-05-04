package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.domain.ServerMachine;
import eu.divum.divumbackend.domain.MinecraftServerInstance;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceStartFailed;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.repositories.ServerMachineRepository;
import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;

import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.HTTPRequestException;

import eu.divum.divumbackend.services.MinecraftServerInstanceService;

import eu.divum.divumbackend.mappers.minecraftserverinstance.MinecraftServerInstanceMapper;

import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceNotFound;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceCreationFailed;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MinecraftServerInstanceServiceImpl implements MinecraftServerInstanceService {
    private final MinecraftServerInstanceRepository minecraftServerRepository;

    private final ServerMachineRepository serverMachineRepository;

    private final UserRepository userRepository;

    private final MinecraftServerInstanceMapper mapper;

    @Qualifier("httpClient")
    private final HttpClient httpClient;

    @Qualifier("snakeCaseJsonMapper")
    private final JsonMapper jsonMapper;

    @Value("${divum-daemon.api-version}/minecraft-servers")
    private String daemonEndpointAddress;

    @Value("${divum-daemon.api-scheme}")
    private String daemonEndpointScheme;

    @Override
    public MinecraftServerInstanceResponse getById(String serverId) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given ID exists."));

        return mapper.mapToResponse(serverInstance);
    }

    @Override
    public MinecraftServerInstanceResponse getByAddress(String address) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findByAddress(address)
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given address exists."));

        return mapper.mapToResponse(serverInstance);
    }

    @Override
    public void start(String serverId) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given ID exists."));

        HttpRequest instanceStartRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        String.format(
                                daemonEndpointScheme + serverInstance.getServerMachine().getIp() + daemonEndpointAddress + "%s/start",
                        serverInstance.getDaemonId()
                )))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<Void> instanceStartResponse =
                    httpClient.send(instanceStartRequest, HttpResponse.BodyHandlers.discarding());

            if (instanceStartResponse.statusCode() < 200 || instanceStartResponse.statusCode() > 299) {
                throw new MinecraftServerInstanceStartFailed("Could not start Minecraft server instance");
            }
        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }

    @Override
    public void stop(String serverId) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given ID exists."));

        HttpRequest instanceStartRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        String.format(
                                daemonEndpointScheme + serverInstance.getServerMachine().getIp() + daemonEndpointAddress + "%s/stop",
                                serverInstance.getDaemonId()
                        )))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<Void> instanceStartResponse =
                    httpClient.send(instanceStartRequest, HttpResponse.BodyHandlers.discarding());

            if (instanceStartResponse.statusCode() < 200 || instanceStartResponse.statusCode() > 299) {
                throw new MinecraftServerInstanceStartFailed("Could not stop Minecraft server instance");
            }
        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }

    @Override
    public void remove(String serverId) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given ID exists."));

        minecraftServerRepository.deleteById(UUID.fromString(serverId));
    }

    @Override
    public String create(MinecraftServerInstanceRequest request) {
        int requiredCpuCores = request.getConfiguration().getCpuCoresLimit();
        int requiredRam = request.getConfiguration().getMemoryLimit();

        List<ServerMachine> availableMachines = serverMachineRepository.findAllAvailable(requiredCpuCores, requiredRam);

        if (availableMachines.isEmpty()) {
            throw new NoAvailableServerMachines("No available server machines for the given RAM and CPU requirements.");
        }

        ServerMachine serverMachine = availableMachines.getFirst();

        String serverConfigurationPayload = jsonMapper.writeValueAsString(request.getConfiguration());

        HttpRequest serverCreationRequest = HttpRequest.newBuilder()
                .uri(URI.create(daemonEndpointScheme + serverMachine.getIp() + daemonEndpointAddress))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serverConfigurationPayload))
                .build();

        try {
            HttpResponse<String> serverCreationResponse = httpClient.send(serverCreationRequest, HttpResponse.BodyHandlers.ofString());

            if (serverCreationResponse.statusCode() < 200 || serverCreationResponse.statusCode() > 299) {
                throw new MinecraftServerInstanceCreationFailed("Minecraft server creation failed.");
            }

            Optional<User> serverCreator = userRepository.findUserByUsername(request.getUsername());

            if (serverCreator.isEmpty()) {
                throw new UserNotFound("User not found.");
            }

            MinecraftServerInstance serverInstanceEntity = mapper.mapToEntity(request);

            serverInstanceEntity.setServerMachine(serverMachine);
            serverInstanceEntity.setOwner(serverCreator.get());

            serverInstanceEntity.setName(request.getConfiguration().getServerName());
            serverInstanceEntity.setAddress(request.getConfiguration().getServerAddress());
            serverInstanceEntity.setDaemonId(serverCreationResponse.body());

            minecraftServerRepository.save(serverInstanceEntity);

            return serverInstanceEntity.getId().toString();
        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }
}
