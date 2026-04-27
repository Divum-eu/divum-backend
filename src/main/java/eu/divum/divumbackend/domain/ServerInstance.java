package eu.divum.divumbackend.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
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

    @Nonnull
    private String address;

    @Nonnull
    private Date createdOn;

    @Nullable
    private Date deletedOn;
}



