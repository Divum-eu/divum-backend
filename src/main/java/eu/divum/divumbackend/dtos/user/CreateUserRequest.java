package eu.divum.divumbackend.dtos.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static eu.divum.divumbackend.constants.UserConstants.*;

public record CreateUserRequest(
        @NotBlank
        @Size(
                min = USERNAME_MIN_LENGTH,
                max = USERNAME_MAX_LENGTH,
                message = "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long.")
        String username,
        @NotBlank @Email String emailAddress,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character."
        )
        @Size(
                min = PASSWORD_MIN_LENGTH,
                max = PASSWORD_MAX_LENGTH,
                message = "Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters long.")
        String password) {}
