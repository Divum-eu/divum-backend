package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.user.UpdateUserResponse;
import eu.divum.divumbackend.services.UserService;

import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;
import eu.divum.divumbackend.dtos.user.CreateUserRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<GetUserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.get(id));
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody CreateUserRequest createUserRequest, UriComponentsBuilder uriComponentsBuilder) {
        String userId = userService.create(createUserRequest);

        URI uri = uriComponentsBuilder.path("/v1/users/{id}").buildAndExpand(userId).toUri();

        return ResponseEntity.created(uri).body(userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(@Valid @RequestBody UpdateUserRequest updateUserRequest, @PathVariable String id) {
        UpdateUserResponse userResponse = userService.update(id, updateUserRequest);

        return ResponseEntity.accepted().body(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
