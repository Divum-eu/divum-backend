package eu.divum.divumbackend.dtos.minecraftserverinstance;

import java.util.UUID;

public record MinecraftServerInstanceResponse(UUID id, MinecraftServerInstanceConfiguration configuration) {
}
