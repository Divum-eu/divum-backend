package eu.divum.divumbackend.controllers;

import eu.divum.divumbackend.dtos.user.GetUserResponse;
import eu.divum.divumbackend.dtos.user.UpdateUserRequest;
import eu.divum.divumbackend.dtos.user.UpdateUserResponse;
import eu.divum.divumbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getUser(
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok(userService.get(userId.toString()));
    }

    // This will be implemented in the future for admins to use
//    @PostMapping
//    public ResponseEntity<String> create(@Valid @RequestBody CreateUserRequest createUserRequest, UriComponentsBuilder uriComponentsBuilder) {
//        String userId = userService.create(createUserRequest);
//
//        URI uri = uriComponentsBuilder.path("/v1/users/{id}").buildAndExpand(userId).toUri();
//
//        return ResponseEntity.created(uri).body(userId);
//    }

    @PatchMapping("/me")
    public ResponseEntity<UpdateUserResponse> update(
            @Valid @RequestBody UpdateUserRequest updateUserRequest,
            @AuthenticationPrincipal UUID userId
            ) {
        UpdateUserResponse userResponse = userService.update(userId.toString(), updateUserRequest);

        return ResponseEntity.accepted().body(userResponse);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UUID userId
    ) {
        userService.delete(userId.toString());

        return ResponseEntity.noContent().build();
    }
}
