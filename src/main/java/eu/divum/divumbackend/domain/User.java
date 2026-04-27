package eu.divum.divumbackend.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(
        name = "Users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "emailAaddress"})
)
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private UUID id;

    @Nonnull
    private String username;

    @Nonnull
    private String emailAddress;

    @Nonnull
    private Date createdOn;

    @Nullable
    private Date deletedOn;
}
