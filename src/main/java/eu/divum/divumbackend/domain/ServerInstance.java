package eu.divum.divumbackend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

import static eu.divum.divumbackend.constants.ServerInstanceConstants.ADDRESS_MAX_LENGTH;
import static eu.divum.divumbackend.constants.ServerInstanceConstants.NAME_MAX_LENGTH;

@Entity
@Table(
        name = "server_instances",
        schema = "divum"
)
@SQLRestriction("deleted_on IS NULL")
@SQLDelete(sql = "UPDATE divum.server_instances SET deleted_on = now() WHERE id = ?")
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
@Getter
@NoArgsConstructor
public class ServerInstance {
    @Id
    @ColumnDefault(value = "uuidv4()")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_machine_id", nullable = false)
    private ServerMachine serverMachine;

    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(name = "address", nullable = false, length = ADDRESS_MAX_LENGTH)
    private String address;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}


