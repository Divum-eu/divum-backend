package eu.divum.divumbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication()
public class DivumBackendApplication {
    static void main(String[] args) {
        SpringApplication.run(DivumBackendApplication.class, args);
    }
}
