package eu.divum.divumbackend.domain;


import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ServerMachines")
@Getter
@Setter
public class ServerMachine {
    @Id
    private UUID id;

    private String ip;

    private int totalRam;

    private int totalCpuCores;

    private int freeRam;

    private int freeCpuCores;
}
