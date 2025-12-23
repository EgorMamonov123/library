package lab.library;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories
public class LibraryApplication {

    public static void main(String[] args) {
        log.info("Starting Library Management System...");
        log.debug("Debug mode is enabled");

        try {
            SpringApplication.run(LibraryApplication.class, args);
            log.info("Library Management System started successfully!");
        } catch (Exception e) {
            log.error("Failed to start Library Management System", e);
            throw e;
        }
    }
}