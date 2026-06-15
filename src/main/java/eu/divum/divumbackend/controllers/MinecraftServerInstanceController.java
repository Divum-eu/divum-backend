package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/minecraft-servers")
@RequiredArgsConstructor
public class MinecraftServerInstanceController {

    private final MinecraftServerInstanceService mcServerInstanceService;

    @GetMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> getMinecraftServerInstanceById(
            @PathVariable String id,
            @AuthenticationPrincipal UUID userId
            ) {
        return ResponseEntity.ok(mcServerInstanceService.getById(id, userId.toString()));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startMinecraftServerInstance(
            @PathVariable String id,
            @AuthenticationPrincipal UUID userId
    ) {
        mcServerInstanceService.start(id, userId.toString());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopMinecraftServerInstance(
            @PathVariable String id,
            @AuthenticationPrincipal UUID userId
    ) {
        mcServerInstanceService.stop(id, userId.toString());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMinecraftServerInstance(
            @PathVariable String id,
            @AuthenticationPrincipal UUID userId
    ) {
        mcServerInstanceService.remove(id, userId.toString());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping
    public ResponseEntity<String> createMinecraftServerInstance(
            @Valid @RequestBody MinecraftServerInstanceRequest request,
            @AuthenticationPrincipal UUID userId,
            UriComponentsBuilder uriBuilder
            ) {
        String serverInstanceId = mcServerInstanceService.create(request, userId.toString());
        URI uri = uriBuilder.path("/v1/minecraft-servers/{id}").buildAndExpand(serverInstanceId).toUri();
        return ResponseEntity.created(uri).body(serverInstanceId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> updateMinecraftServerInstance(
            @PathVariable String id,
            @Valid @RequestBody MinecraftServerInstanceRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        MinecraftServerInstanceResponse response = mcServerInstanceService.update(id, request, userId.toString());
        return ResponseEntity.accepted().body(response);
    }
}
