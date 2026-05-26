package eu.divum.divumbackend.dtos.minecraftserverinstance;

import eu.divum.divumbackend.domain.enums.MinecraftServerDifficulty;
import eu.divum.divumbackend.domain.enums.MinecraftServerMode;
import eu.divum.divumbackend.domain.enums.MinecraftServerType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
public class MinecraftServerInstanceConfiguration {
    @Min(value = 512, message = "Minimum RAM is 512 MB.")
    @Max(value = 24576, message = "Maximum RAM is 24576 MB.")
    @NotNull(message = "Memory limit is required.")
    private Integer memoryLimit;

    @DecimalMin(value = "0.5", message = "Minimum of 0.5 CPU cores is required.")
    @DecimalMax(value = "8", message = "Maximum of 8 CPU cores is allowed.")
    @NotNull(message = "CPU cores limit is required.")
    private Float cpuCoresLimit;

    @AssertTrue(message = "Accepting the EULA is required.")
    @NotNull(message = "Accepting the EULA is required.")
    private Boolean eula;

    @NotBlank(message = "Server version is required.")
    private String version;

    @NotNull(message = "Server type is required.")
    private MinecraftServerType type;

    private String motd = "Brought to you by Divum.eu!";

    @NotNull(message = "Difficulty is required.")
    private MinecraftServerDifficulty difficulty;

    @NotNull(message = "Server mode is required.")
    private MinecraftServerMode mode;

    private String level = "world";

    @NotNull(message = "Must specify online mode.")
    private Boolean onlineMode;

    private String resourcePack;

    private String resourcePackSha1;

    private Boolean resourcePackEnforce = Boolean.FALSE;

    private Boolean enableWhitelist = Boolean.FALSE;

    private List<String> whitelist;

    private Boolean overrideWhitelist = Boolean.FALSE;

    private Boolean enableRcon = Boolean.FALSE;

    @NotBlank(message = "Server RCON password is required.")
    private String rconPassword;

    private Boolean broadcastRconToOps = Boolean.FALSE;

    private List<String> ops;

    private Integer opPermissionLevel;

    private String seed;

    private Boolean pvp;

    private String serverName;

    @NotBlank(message = "Server address is required.")
    private String serverAddress;
}
