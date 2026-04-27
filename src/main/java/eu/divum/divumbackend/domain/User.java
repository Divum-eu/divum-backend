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
        name = "Users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "email_address"})
)
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "created_on", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}
