package eu.divum.divumbackend;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceRequest;
import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceResponse;
import eu.divum.divumbackend.services.MinecraftServerInstanceService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/minecraft-servers")
@RequiredArgsConstructor
public class TestController {
    private final MinecraftServerInstanceService service;

    @GetMapping("/{id}")
    public ResponseEntity<MinecraftServerInstanceResponse> getServerTest(@PathVariable String id) {
        MinecraftServerInstanceResponse serverInstance = service.getById(id);

        return ResponseEntity.ok(serverInstance);
    }

    @PostMapping
    public ResponseEntity<String> createServer(@RequestBody MinecraftServerInstanceRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping()
    public ResponseEntity<MinecraftServerInstanceResponse> getServerTestByAddress(@RequestBody String address) {
        MinecraftServerInstanceResponse serverInstance = service.getByAddress(address);

        return ResponseEntity.ok(serverInstance);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{id}/start")
    public void startServer(@PathVariable String id) {
        service.start(id);
    }

    public void stopServer(@PathVariable String id) {
        service.stop(id);
    }
}
