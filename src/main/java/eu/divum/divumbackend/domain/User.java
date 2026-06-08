package eu.divum.divumbackend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

import java.util.UUID;

import static eu.divum.divumbackend.constants.UserConstants.USERNAME_MAX_LENGTH;
import static eu.divum.divumbackend.constants.UserConstants.EMAIL_ADDRESS_MAX_LENGTH;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "email_address"}),
        schema = "divum"
)
@SQLRestriction("deleted_on IS NULL")
@SQLDelete(sql = "UPDATE divum.users SET deleted_on = now() WHERE id = ?")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @ColumnDefault(value = "uuidv4()")
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "username", nullable = false, length = USERNAME_MAX_LENGTH)
    private String username;

    @Column(name = "email_address", nullable = false, length = EMAIL_ADDRESS_MAX_LENGTH)
    private String emailAddress;

    @Column(name = "password_data", nullable = false)
    private String passwordData;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}
