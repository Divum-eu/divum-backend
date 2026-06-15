package eu.divum.divumbackend.domain;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "minecraft_server_instances", schema = "divum")
@SQLDelete(sql = "UPDATE divum.minecraft_server_instances SET daemon_id = daemon_id WHERE id = ? " +
        "--no-op update statement required by the joined table inheritance type")
@Getter
@Setter
@NoArgsConstructor
public class MinecraftServerInstance extends ServerInstance {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "json", nullable = false)
    private MinecraftServerInstanceConfiguration configuration;

    @Column(name = "daemon_id", nullable = false)
    private String daemonId;
}
