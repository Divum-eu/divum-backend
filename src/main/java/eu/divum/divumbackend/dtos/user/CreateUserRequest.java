package eu.divum.divumbackend.dtos.user;

import jakarta.validation.Valid;

public record CreateUserRequest(@Valid String username, @Valid String emailAddress, @Valid String password) {
}
