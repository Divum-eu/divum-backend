package eu.divum.divumbackend.domain;


import jakarta.annotation.Nonnull;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ServerMachines", uniqueConstraints = @UniqueConstraint(columnNames = {"ip"}))
@Getter
@Setter
@NoArgsConstructor
public class ServerMachine {
    @Id
    private UUID id;

    @Nonnull
    private String ip;

    private int totalRam;

    private int totalCpuCores;

    private int freeRam;

    private int freeCpuCores;
}
