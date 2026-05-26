package eu.divum.divumbackend.dtos.minecraftserverinstance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MinecraftServerInstanceRequest(
        @NotBlank(message = "Username is required.")
        String username,

        @Valid
        @NotNull(message = "Configuration is required.")
        MinecraftServerInstanceConfiguration configuration) {}
