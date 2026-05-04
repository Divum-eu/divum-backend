package eu.divum.divumbackend.domain;

import eu.divum.divumbackend.constants.DomainConstants;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
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
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "email_address"}),
        schema = "divum"
)
@SQLRestriction("deleted_on IS NOT NULL")
@SQLDelete(sql = "UPDATE users SET deleted_on = now() WHERE id = ?")
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

    @Column(name = "username", nullable = false, length = DomainConstants.USER_USERNAME_MAX_LENGTH)
    private String username;

    @Column(name = "email_address", nullable = false, length = DomainConstants.USER_EMAIL_ADDRESS_MAX_LENGTH)
    private String emailAddress;

    @Column(name = "password_data", nullable = false)
    private String passwordData;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "deleted_on")
    private Instant deletedOn;
}
