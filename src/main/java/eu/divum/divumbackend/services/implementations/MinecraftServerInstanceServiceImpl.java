package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.domain.ServerMachine;
import eu.divum.divumbackend.domain.MinecraftServerInstance;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceStopFailed;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.repositories.ServerMachineRepository;
import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;

import eu.divum.divumbackend.services.MinecraftServerInstanceService;

import eu.divum.divumbackend.mappers.minecraftserverinstance.MinecraftServerInstanceMapper;

import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.HTTPRequestException;
import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceNotFound;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.MinecraftServerInstanceStartFailed;
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

        String daemonStartApiUrl =
                String.format(
                        daemonEndpointScheme + serverInstance.getServerMachine().getIp() + daemonEndpointAddress + "/%s/start",
                        serverInstance.getDaemonId());

        HttpRequest instanceStartRequest = HttpRequest.newBuilder()
                .uri(URI.create(daemonStartApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<Void> instanceStartResponse =
                    httpClient.send(instanceStartRequest, HttpResponse.BodyHandlers.discarding());

            if (instanceStartResponse.statusCode() == 404) {
                throw new MinecraftServerInstanceNotFound("No server with the given daemon ID exists.");
            } else if (instanceStartResponse.statusCode() < 200 || instanceStartResponse.statusCode() > 299) {
                throw new MinecraftServerInstanceStartFailed("Could not start Minecraft server instance.");
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

        String daemonStopApiUrl =
                String.format(
                        daemonEndpointScheme + serverInstance.getServerMachine().getIp() + daemonEndpointAddress + "/%s/stop",
                        serverInstance.getDaemonId());

        HttpRequest instanceStartRequest = HttpRequest.newBuilder()
                .uri(URI.create(daemonStopApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<Void> instanceStartResponse =
                    httpClient.send(instanceStartRequest, HttpResponse.BodyHandlers.discarding());

            if (instanceStartResponse.statusCode() == 404) {
                throw new MinecraftServerInstanceNotFound("No server with the given daemon ID exists.");
            } else if (instanceStartResponse.statusCode() < 200 || instanceStartResponse.statusCode() > 299) {
                throw new MinecraftServerInstanceStopFailed("Could not stop Minecraft server instance.");
            }

        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }

    @Override
    public void remove(String serverId) {
        minecraftServerRepository.deleteById(UUID.fromString(serverId));
    }

    @Override
    public String create(MinecraftServerInstanceRequest request) {
        int requiredCpuCores = request.configuration().getCpuCoresLimit();
        int requiredRam = request.configuration().getMemoryLimit();

        List<ServerMachine> availableMachines = serverMachineRepository.findAllAvailable(requiredCpuCores, requiredRam);

        if (availableMachines.isEmpty()) {
            throw new NoAvailableServerMachines("No server machines for the given RAM and CPU requirements are available.");
        }

        Optional<User> serverCreator = userRepository.findUserByUsername(request.username());

        if (serverCreator.isEmpty()) {
            throw new UserNotFound("No user with the given username exists.");
        }

        ServerMachine serverMachine = availableMachines.getFirst();

        String serverConfigurationPayload = jsonMapper.writeValueAsString(request.configuration());

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

            String daemonId = jsonMapper.readValue(serverCreationResponse.body(), String.class);

            if (daemonId == null || daemonId.isEmpty()) {
                throw new MinecraftServerInstanceCreationFailed("The Divum Daemon didn't return an ID for the server.");
            }

            MinecraftServerInstance serverInstanceEntity = mapper.mapToEntity(request);

            serverInstanceEntity.setServerMachine(serverMachine);
            serverInstanceEntity.setOwner(serverCreator.get());

            serverInstanceEntity.setName(request.configuration().getServerName());
            serverInstanceEntity.setAddress(request.configuration().getServerAddress());

            serverInstanceEntity.setDaemonId(daemonId);

            minecraftServerRepository.save(serverInstanceEntity);

            return serverInstanceEntity.getId().toString();
        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }
}
