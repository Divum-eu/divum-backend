package eu.divum.divumbackend.domain;


import eu.divum.divumbackend.constants.DomainConstants;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "server_machines", uniqueConstraints = @UniqueConstraint(columnNames = {"ip"}), schema = "divum")
@SQLRestriction("deleted_on IS NULL")
@SQLDelete(sql = "UPDATE divum.server_machines SET deleted_on = now() WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
public class ServerMachine {
    @Id
    @ColumnDefault(value = "uuidv4()")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "ip", nullable = false, length = DomainConstants.SERVER_MACHINE_IP_MAX_LENGTH)
    private String ip;

    @Column(name = "total_ram_mb", nullable = false)
    private int totalRamMb;

    @Column(name = "total_cpu_cores", nullable = false)
    private float totalCpuCores;

    @Column(name = "free_ram_mb", nullable = false)
    private int freeRamMb;

    @Column(name = "free_cpu_cores", nullable = false)
    private float freeCpuCores;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}
