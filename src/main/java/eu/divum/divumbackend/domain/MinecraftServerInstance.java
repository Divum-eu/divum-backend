package eu.divum.divumbackend.domain;

import eu.divum.divumbackend.dtos.minecraftserverinstance.MinecraftServerInstanceConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "minecraft_server_instances", schema = "divum")
@SQLRestriction("deleted_on IS NOT NULL")
@SQLDelete(sql = "UPDATE minecraft_server_instances SET deleted_on = now() WHERE id = ?")
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
