package eu.divum.divumbackend.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(

        @NotBlank(message = "Username is required.")
        String username,

        @NotBlank(message = "Email is required.")
        @Email(message = "Not a valid email.")
        String email,

        @NotBlank(message = "Password is required.")
        String password
) {
}
