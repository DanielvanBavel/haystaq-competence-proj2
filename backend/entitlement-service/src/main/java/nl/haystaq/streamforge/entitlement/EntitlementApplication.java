package nl.haystaq.streamforge.entitlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "nl.haystaq.streamforge")
public class EntitlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntitlementApplication.class, args);
    }
}
