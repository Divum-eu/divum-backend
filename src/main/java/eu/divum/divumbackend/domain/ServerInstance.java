package eu.divum.divumbackend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "ServerInstances",
        uniqueConstraints = @UniqueConstraint(columnNames = "address")
)
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
@Getter
@NoArgsConstructor
public class ServerInstance {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name="owner_id", nullable = false)
    private User ownerId;

    @ManyToOne
    @JoinColumn(name="server_machine_id", nullable = false)
    private ServerMachine serverMachine;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "created_on", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}



