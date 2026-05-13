package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.ErrorDto;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import eu.divum.divumbackend.exceptions.HTTPRequestException;
import eu.divum.divumbackend.exceptions.minecraftserverinstance.*;
import eu.divum.divumbackend.exceptions.servermachine.NoAvailableServerMachines;
import eu.divum.divumbackend.exceptions.servermachine.NotEnoughServerResources;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import eu.divum.divumbackend.services.implementations.MinecraftServerInstanceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/mc-server-instances")
@RequiredArgsConstructor
public class MinecraftServerInstanceController {

    private final MinecraftServerInstanceService mcServerInstanceService;

    @GetMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> getMCServerInstanceById(
            @PathVariable String id
            ) {
        return ResponseEntity.ok(mcServerInstanceService.getById(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startMCServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.start(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopMCServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.stop(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMCServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.remove(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping
    public ResponseEntity<String> createMCServerInstance(
            @Valid @RequestBody MinecraftServerInstanceRequest request,
            UriComponentsBuilder uriBuilder
            ) {
        String serverInstanceId = mcServerInstanceService.create(request);
        URI uri = uriBuilder.path("/v1/mc-server-instances/{id}").buildAndExpand(serverInstanceId).toUri();
        return ResponseEntity.created(uri).body(serverInstanceId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> updateMCServerInstance(
            @PathVariable String id,
            @Valid @RequestBody MinecraftServerInstanceRequest request
    ) {
        MinecraftServerInstanceResponse response = mcServerInstanceService.update(id, request);
        return ResponseEntity.accepted().body(response);
    }

    @ExceptionHandler(HTTPRequestException.class)
    public ResponseEntity<ErrorDto> handleHTTPRequestException() {
        return ResponseEntity.internalServerError().body(new ErrorDto("Something unexpected happened."));
    }

    @ExceptionHandler(MinecraftServerInstanceNotFound.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Minecraft server instance not found."));
    }

    @ExceptionHandler(MinecraftServerInstanceStartFailed.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceStartFailed() {
        return ResponseEntity.internalServerError().body(new ErrorDto("Couldn't start Minecraft server instance."));
    }

    @ExceptionHandler(MinecraftServerInstanceStopFailed.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceStopFailed() {
        return ResponseEntity.internalServerError().body(new ErrorDto("Couldn't stop Minecraft server instance."));
    }

    @ExceptionHandler(MinecraftServerInstanceDeleteFailed.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceDeleteFailed() {
        return ResponseEntity.internalServerError().body(new ErrorDto("Couldn't delete Minecraft server instance."));
    }

    @ExceptionHandler(MinecraftServerInstanceWithSameAddressExists.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceWithSameAddressExistsException(MinecraftServerInstanceWithSameAddressExists e) {
        return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage().isBlank() ? "A Minecraft server instance with the given address already exists." : e.getMessage()));
    }

    @ExceptionHandler(NoAvailableServerMachines.class)
    public ResponseEntity<ErrorDto> handleNoAvailableServerMachinesException(NoAvailableServerMachines e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getMessage().isBlank() ? "No server machines for the given RAM and CPU requirements are available." : e.getMessage()));
    }

    @ExceptionHandler(MinecraftServerInstanceCreationFailed.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceCreationFailed(MinecraftServerInstanceCreationFailed e) {
        return ResponseEntity.internalServerError().body(new ErrorDto(e.getMessage().isBlank() ? "Minecraft server instance creation failed." : e.getMessage()));
    }

    @ExceptionHandler(NotEnoughServerResources.class)
    public ResponseEntity<ErrorDto> handleNotEnoughServerResources(NotEnoughServerResources e) {
        return ResponseEntity.badRequest().body(new ErrorDto(e.getMessage().isBlank() ? "Not enough server resources to satisfy request." : e.getMessage()));
    }

    @ExceptionHandler(MinecraftServerInstanceUpdateFailed.class)
    public ResponseEntity<ErrorDto> handleMinecraftServerInstanceUpdateFailed(MinecraftServerInstanceUpdateFailed e) {
        return ResponseEntity.internalServerError().body(new ErrorDto(e.getMessage().isBlank() ? "Couldn't update Minecraft instance.": e.getMessage()));
    }
}
