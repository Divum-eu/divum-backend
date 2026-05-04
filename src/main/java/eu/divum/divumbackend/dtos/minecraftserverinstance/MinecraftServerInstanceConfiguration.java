package eu.divum.divumbackend.dtos.minecraftserverinstance;

import lombok.*;

import java.util.List;

@Data
public class MinecraftServerInstanceConfiguration {
    private int memoryLimit;

    private int cpuCoresLimit;

    private boolean eula;

    private String version;

    private String type;

    private String motd;

    private String difficulty;

    private String mode;

    private String level;

    private boolean onlineMode;

    private String resourcePack;

    private String resourcePackSha1;

    private boolean resourcePackEnforce;

    private boolean enableWhitelist;

    private List<String> whitelist;

    private boolean overrideWhitelist;

    private boolean enableRcon;

    private String rconPassword;

    private boolean broadcastRconToOps;

    private List<String> ops;

    private int opPermissionLevel;

    private String seed;

    private boolean pvp;

    private String serverName;

    private String serverAddress;
}
