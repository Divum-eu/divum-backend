package eu.divum.divumbackend.domain;

import eu.divum.divumbackend.constants.DomainConstants;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.*;

import java.time.Instant;

import java.util.UUID;


@Entity
@Table(
        name = "server_instances",
        uniqueConstraints = @UniqueConstraint(columnNames = "address"),
        schema = "divum"
)
@SQLRestriction("deleted_on IS NOT NULL")
@SQLDelete(sql = "UPDATE server_instances SET deleted_on = now() WHERE id = ?")
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

    @ManyToOne
    @JoinColumn(name="owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name="server_machine_id", nullable = false)
    private ServerMachine serverMachine;

    @Column(name = "name", nullable = false, length = DomainConstants.SERVER_INSTANCE_NAME_MAX_LENGTH)
    private String name;

    @Column(name = "address", nullable = false, length = DomainConstants.SERVER_INSTANCE_ADDRESS_MAX_LENGTH)
    private String address;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}


