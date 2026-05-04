package eu.divum.divumbackend.dtos.minecraftserverinstance;

import lombok.Data;

@Data
public class MinecraftServerInstanceRequest {
    private String username;

    private MinecraftServerInstanceConfiguration configuration;
}
