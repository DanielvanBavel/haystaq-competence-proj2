package nl.haystaq.streamforge.gateway;

import nl.haystaq.streamforge.common.DownstreamClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication(scanBasePackages = "nl.haystaq.streamforge")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * De timeout richting de catalogus staat op 1,5 seconde. Dat was ruim
     * voldoende toen de zoekfunctie werd gebouwd.
     */
    @Bean
    DownstreamClient catalogClient(@Value("${streamforge.catalog-url}") String baseUrl,
                                   @Value("${streamforge.catalog-timeout-ms:1500}") long timeoutMillis) {
        return new DownstreamClient("catalog", baseUrl, Duration.ofSeconds(2), Duration.ofMillis(timeoutMillis));
    }

    @Bean
    DownstreamClient playbackClient(@Value("${streamforge.playback-url}") String baseUrl) {
        return new DownstreamClient("playback", baseUrl, Duration.ofSeconds(2), Duration.ofSeconds(15));
    }

    @Bean
    DownstreamClient entitlementClient(@Value("${streamforge.entitlement-url}") String baseUrl) {
        return new DownstreamClient("entitlement", baseUrl, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }
}
