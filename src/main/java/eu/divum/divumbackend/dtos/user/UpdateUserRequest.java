package eu.divum.divumbackend.dtos.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static eu.divum.divumbackend.constants.UserConstants.USERNAME_MAX_LENGTH;
import static eu.divum.divumbackend.constants.UserConstants.USERNAME_MIN_LENGTH;

public record UpdateUserRequest(
        @NotBlank @Size(
                min = USERNAME_MIN_LENGTH,
                max = USERNAME_MAX_LENGTH,
                message = "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long.")
        String username) {
}
