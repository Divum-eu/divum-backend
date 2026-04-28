package eu.divum.divumbackend.domain;


import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "server_machines", uniqueConstraints = @UniqueConstraint(columnNames = {"ip"}))
@Getter
@Setter
@NoArgsConstructor
public class ServerMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID id;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "total_ram")
    private int totalRam;

    @Column(name = "total_cpu_cores")
    private int totalCpuCores;

    @Column(name = "free_ram")
    private int freeRam;

    @Column(name = "free_cpu_cores")
    private int freeCpuCores;
}
