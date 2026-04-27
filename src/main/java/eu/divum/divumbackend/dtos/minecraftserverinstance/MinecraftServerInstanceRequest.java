package eu.divum.divumbackend.dtos.minecraftserverinstance;

public record MinecraftServerInstanceRequest(String serverName, String serverDomain,
                                             MinecraftServerInstanceConfiguration minecraftServerConfig) {
}
