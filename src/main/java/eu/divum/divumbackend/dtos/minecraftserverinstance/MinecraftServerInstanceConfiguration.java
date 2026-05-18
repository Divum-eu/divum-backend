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
    private int memoryLimit;

    @DecimalMin(value = "0.5", message = "Minimum of 0.5 CPU cores is required.")
    @DecimalMax(value = "8", message = "Maximum of 8 CPU cores is allowed.")
    @NotNull(message = "CPU cores limit is required.")
    private float cpuCoresLimit;

    @AssertTrue
    @NotNull(message = "Accepting the EULA is required.")
    private boolean eula;

    @NotBlank(message = "Server version is required.")
    private String version;

    @NotBlank(message = "Server type is required.")
    private MinecraftServerType type;

    private String motd;

    @NotBlank(message = "Must specify difficulty")
    private MinecraftServerDifficulty difficulty;

    @NotBlank(message = "Server mode is required.")
    private MinecraftServerMode mode;

    private String level;

    @NotNull(message = "Must specify online mode.")
    private boolean onlineMode;

    private String resourcePack;

    private String resourcePackSha1;

    private boolean resourcePackEnforce;

    private boolean enableWhitelist;

    private List<String> whitelist;

    private boolean overrideWhitelist;

    private boolean enableRcon;

    @NotBlank(message = "Server RCON password is required.")
    private String rconPassword;

    private boolean broadcastRconToOps;

    private List<String> ops;

    private int opPermissionLevel;

    private String seed;

    private boolean pvp;

    private String serverName;

    @NotBlank(message = "Server address is required.")
    private String serverAddress;
}
