package eu.divum.divumbackend.dtos.minecraftserverinstance;

public record MinecraftServerInstanceResponse(String serverId, String serverName, MinecraftServerInstanceConfiguration configuration) {
}
