package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.domain.ServerMachine;
import eu.divum.divumbackend.domain.MinecraftServerInstance;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;

import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.repositories.ServerMachineRepository;
import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;

import eu.divum.divumbackend.services.DNSRecordManager;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;

import eu.divum.divumbackend.mappers.minecraftserverinstance.MinecraftServerInstanceMapper;

import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.exceptions.HTTPRequestException;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.*;
import eu.divum.divumbackend.exceptions.cloudflare.CloudflareAPIException;
import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;

import jakarta.transaction.Transactional;

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

@Service
@RequiredArgsConstructor
public class MinecraftServerInstanceServiceImpl implements MinecraftServerInstanceService {
    private final MinecraftServerInstanceRepository minecraftServerRepository;

    private final ServerMachineRepository serverMachineRepository;

    private final UserRepository userRepository;

    private final MinecraftServerInstanceMapper dtoMapper;

    private final DNSRecordManager dnsRecordManager;

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

        return dtoMapper.mapToResponse(serverInstance);
    }

    @Override
    public MinecraftServerInstanceResponse getByAddress(String address) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findByAddress(address)
                .orElseThrow(() ->
                        new MinecraftServerInstanceNotFound("No Minecraft server instance with the given address exists."));

        return dtoMapper.mapToResponse(serverInstance);
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
    @Transactional
    public String create(MinecraftServerInstanceRequest request) {
        int requiredCpuCores = request.configuration().getCpuCoresLimit();
        int requiredRamMb = request.configuration().getMemoryLimit();

        if (minecraftServerRepository.existsByAddress(request.configuration().getServerAddress())) {
            throw new MinecraftServerInstanceWithSameAddressExists("A Minecraft server instance with the given address already exists.");
        }

        List<ServerMachine> availableMachines = serverMachineRepository.findAllAvailable(requiredCpuCores, requiredRamMb);

        if (availableMachines.isEmpty()) {
            throw new NoAvailableServerMachines("No server machines for the given RAM and CPU requirements are available.");
        }

        User serverCreator = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new UserNotFound("No user with the given username exists."));

        ServerMachine serverMachine = availableMachines.getFirst();

        String registeredServerDomain = "";

        try {
            registeredServerDomain =
                    dnsRecordManager.create(request.configuration().getServerAddress(), serverMachine.getIp());

            String serverConfigurationPayload = jsonMapper.writeValueAsString(request.configuration());

            HttpRequest serverCreationRequest = HttpRequest.newBuilder()
                    .uri(URI.create(daemonEndpointScheme + serverMachine.getIp() + daemonEndpointAddress))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(serverConfigurationPayload))
                    .build();

            HttpResponse<String> serverCreationResponse =
                    httpClient.send(serverCreationRequest, HttpResponse.BodyHandlers.ofString());

            if (serverCreationResponse.statusCode() < 200 || serverCreationResponse.statusCode() > 299) {
                boolean _ = dnsRecordManager.delete(registeredServerDomain);

                throw new MinecraftServerInstanceCreationFailed("Minecraft server instance creation failed.");
            }

            String daemonId = jsonMapper.readValue(serverCreationResponse.body(), String.class);

            MinecraftServerInstance serverInstanceEntity = dtoMapper.mapToEntity(request);

            serverInstanceEntity.setDaemonId(daemonId);
            serverInstanceEntity.setOwner(serverCreator);
            serverInstanceEntity.setServerMachine(serverMachine);
            serverInstanceEntity.setAddress(registeredServerDomain);
            serverInstanceEntity.setName(request.configuration().getServerName());
            serverInstanceEntity.getConfiguration().setServerAddress(registeredServerDomain);

            minecraftServerRepository.save(serverInstanceEntity);

            return serverInstanceEntity.getId().toString();

        } catch (IllegalArgumentException | CloudflareAPIException exception) {
            throw new MinecraftServerInstanceCreationFailed(exception.getMessage());

        } catch (IOException | InterruptedException | HTTPRequestException exception) {
            boolean _ = dnsRecordManager.delete(registeredServerDomain);

            throw new HTTPRequestException();
        }
    }
}
