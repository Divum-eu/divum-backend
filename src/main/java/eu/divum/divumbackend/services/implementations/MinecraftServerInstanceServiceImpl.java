package eu.divum.divumbackend.services.implementations;

import eu.divum.divumbackend.domain.MinecraftServerInstance;
import eu.divum.divumbackend.domain.ServerMachine;
import eu.divum.divumbackend.domain.User;
import eu.divum.divumbackend.dtos.minecraftserverinstance.DaemonConnectionInfo;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceConfiguration;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import eu.divum.divumbackend.exceptions.HTTPRequestException;
import eu.divum.divumbackend.exceptions.cloudflare.CloudflareAPIException;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.*;
import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;
import eu.divum.divumbackend.exceptions.servermachine.NotEnoughServerResources;
import eu.divum.divumbackend.exceptions.user.UserNotFound;
import eu.divum.divumbackend.mappers.minecraftserverinstance.MinecraftServerInstanceMapper;
import eu.divum.divumbackend.repositories.MinecraftServerInstanceRepository;
import eu.divum.divumbackend.repositories.ServerMachineRepository;
import eu.divum.divumbackend.repositories.UserRepository;
import eu.divum.divumbackend.services.DNSRecordManager;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    public DaemonConnectionInfo getDaemonConnectionInfoById(String serverId) {
        return minecraftServerRepository.findDaemonConnectionInfoById(UUID.fromString(serverId))
                .orElseThrow(() -> new MinecraftServerInstanceNotFound("Couldn't find Minecraft server instance with id " + serverId));
    }

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
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() -> new MinecraftServerInstanceNotFound("Couldn't find Minecraft instance with the given ID."));

        String daemonDeleteUrl = String.format(
                daemonEndpointScheme + serverInstance.getServerMachine().getIp() + daemonEndpointAddress + "/%s",
                serverInstance.getDaemonId());

        HttpRequest instanceDeleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(daemonDeleteUrl))
                .DELETE()
                .build();

        try {
            HttpResponse<Void> instanceDeleteResponse =
                    httpClient.send(instanceDeleteRequest, HttpResponse.BodyHandlers.discarding());

            if (instanceDeleteResponse.statusCode() != 204) {
                throw new MinecraftServerInstanceDeleteFailed("Couldn't delete the Minecraft server instance.");
            }

        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }

        try {
            dnsRecordManager.delete(serverInstance.getAddress());
        } catch (HTTPRequestException e) {
            // TODO: log dangling domain
            throw e;
        }

        // Give the server machine it's resources back
        ServerMachine serverMachine = serverInstance.getServerMachine();
        serverMachine.setFreeCpuCores(serverMachine.getFreeCpuCores() + serverInstance.getConfiguration().getCpuCoresLimit());
        serverMachine.setFreeRamMb(serverMachine.getFreeRamMb() + serverInstance.getConfiguration().getMemoryLimit());

        minecraftServerRepository.deleteById(UUID.fromString(serverId));
    }

    @Override
    public String create(MinecraftServerInstanceRequest request) {
        float requiredCpuCores = request.configuration().getCpuCoresLimit();
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

        serverMachine.setFreeCpuCores(serverMachine.getFreeCpuCores() - requiredCpuCores);
        serverMachine.setFreeRamMb(serverMachine.getFreeRamMb() - requiredRamMb);

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

    @Override
    public MinecraftServerInstanceResponse update(String serverId, MinecraftServerInstanceRequest request) {
        MinecraftServerInstance serverInstance = minecraftServerRepository.findById(UUID.fromString(serverId))
                .orElseThrow(() -> new MinecraftServerInstanceNotFound("Minecraft server instance not found."));

        MinecraftServerInstanceConfiguration oldConfiguration = serverInstance.getConfiguration();
        MinecraftServerInstanceConfiguration newConfiguration = request.configuration();

        // Check for request to change server resources
        float oldCpuLimit = oldConfiguration.getCpuCoresLimit();
        float newCpuLimit = newConfiguration.getCpuCoresLimit();
        int oldMemoryLimit = oldConfiguration.getMemoryLimit();
        int newMemoryLimit = newConfiguration.getMemoryLimit();
        if (oldCpuLimit != newCpuLimit || oldMemoryLimit != newMemoryLimit) {
            if (newCpuLimit > serverInstance.getServerMachine().getFreeCpuCores() + oldCpuLimit) {
                throw new NotEnoughServerResources("Can't satisfy the given CPU cores.");
            }
            if (newMemoryLimit > serverInstance.getServerMachine().getFreeRamMb() + oldMemoryLimit) {
                throw new NotEnoughServerResources("Can't satisfy the given memory limit.");
            }

            serverInstance.getConfiguration().setCpuCoresLimit(newCpuLimit);
            serverInstance.getConfiguration().setMemoryLimit(newMemoryLimit);
        }

        String oldAddress = oldConfiguration.getServerAddress();
        String newAddress = newConfiguration.getServerAddress().strip();
        if (!oldAddress.equals(newAddress)) {
            serverInstance.setAddress(newAddress);
            serverInstance.getConfiguration().setServerAddress(newAddress);
        }

        String daemonUpdateUrl = String.format("%s%s%s/%s",
                daemonEndpointScheme, serverInstance.getServerMachine().getIp(), daemonEndpointAddress, serverInstance.getDaemonId());

        HttpRequest daemonUpdateRequest = HttpRequest.newBuilder()
                .uri(URI.create(daemonUpdateUrl))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(request.configuration())))
                .build();

        try {
            HttpResponse<String> daemonUpdateResponse = httpClient.send(daemonUpdateRequest, HttpResponse.BodyHandlers.ofString());

            if (daemonUpdateResponse.statusCode() != 204) {
                throw new MinecraftServerInstanceUpdateFailed("Couldn't update Minecraft instance.");
            }

            if (!oldAddress.equals(newAddress)) {
                dnsRecordManager.create(newAddress, serverInstance.getServerMachine().getIp());
                dnsRecordManager.delete(oldAddress);
            }

            serverInstance.setConfiguration(newConfiguration);
            minecraftServerRepository.save(serverInstance);
            return new MinecraftServerInstanceResponse(serverInstance.getId(), newConfiguration);

        } catch (IOException | InterruptedException exception) {
            throw new HTTPRequestException();
        }
    }
}
