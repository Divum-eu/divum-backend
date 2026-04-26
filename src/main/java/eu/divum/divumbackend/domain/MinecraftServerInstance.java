package eu.divum.divumbackend.domain;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MinecraftServerInstances")
@Getter
@Setter
public class MinecraftServerInstance extends ServerInstance {

}
