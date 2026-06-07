package eu.divum.divumbackend.dtos.user;


import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

import static eu.divum.divumbackend.constants.DomainConstants.USER_USERNAME_MAX_LENGTH;

public record UpdateUserRequest(
        @NotBlank @Size(
                max = USER_USERNAME_MAX_LENGTH,
                message = "Username cannot exceed " + USER_USERNAME_MAX_LENGTH + "characters.")
        String username) {
}
