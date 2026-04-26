package eu.divum.divumbackend.domain;

import jakarta.persistence.*;
import jakarta.annotation.Nullable;

import lombok.Getter;
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
public class ServerInstance {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID ownerId;

    private UUID serverMachineId;

    private String address;

    private Date createdOn;

    @Nullable
    private Date deletedOn;
}



