package nl.haystaq.streamforge.playback;

import nl.haystaq.streamforge.common.DownstreamClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication(scanBasePackages = "nl.haystaq.streamforge")
public class PlaybackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaybackApplication.class, args);
    }

    @Bean
    DownstreamClient catalogClient(@Value("${streamforge.catalog-url}") String baseUrl) {
        return new DownstreamClient("catalog", baseUrl, Duration.ofSeconds(2), Duration.ofSeconds(10));
    }

    @Bean
    DownstreamClient entitlementClient(@Value("${streamforge.entitlement-url}") String baseUrl) {
        return new DownstreamClient("entitlement", baseUrl, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }
}
