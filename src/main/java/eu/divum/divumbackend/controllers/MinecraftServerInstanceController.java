package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/minecraft-servers")
@RequiredArgsConstructor
public class MinecraftServerInstanceController {

    private final MinecraftServerInstanceService mcServerInstanceService;

    @GetMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> getMinecraftServerInstanceById(
            @PathVariable String id
            ) {
        return ResponseEntity.ok(mcServerInstanceService.getById(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startMinecraftServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.start(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stopMinecraftServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.stop(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMinecraftServerInstance(
            @PathVariable String id
    ) {
        mcServerInstanceService.remove(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping
    public ResponseEntity<String> createMinecraftServerInstance(
            @Valid @RequestBody MinecraftServerInstanceRequest request,
            UriComponentsBuilder uriBuilder
            ) {
        String serverInstanceId = mcServerInstanceService.create(request);
        URI uri = uriBuilder.path("/v1/minecraft-servers/{id}").buildAndExpand(serverInstanceId).toUri();
        return ResponseEntity.created(uri).body(serverInstanceId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> updateMinecraftServerInstance(
            @PathVariable String id,
            @Valid @RequestBody MinecraftServerInstanceRequest request
    ) {
        MinecraftServerInstanceResponse response = mcServerInstanceService.update(id, request);
        return ResponseEntity.accepted().body(response);
    }
}
