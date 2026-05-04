package eu.divum.divumbackend.domain;


import eu.divum.divumbackend.constants.DomainConstants;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "server_machines", uniqueConstraints = @UniqueConstraint(columnNames = {"ip"}), schema = "divum")
@SQLRestriction("deleted_on IS NOT NULL")
@SQLDelete(sql = "UPDATE server_machines SET deleted_on = now() WHERE id = ?")
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

    @Column(name = "total_ram")
    private int totalRam;

    @Column(name = "total_cpu_cores")
    private int totalCpuCores;

    @Column(name = "free_ram")
    private int freeRam;

    @Column(name = "free_cpu_cores")
    private int freeCpuCores;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}
