package nl.haystaq.streamforge.entitlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/** Aggregate root: het abonnement van een kijker. */
@Entity
@Table(name = "subscription")
public class Subscription {

    public enum Plan {
        BASIC(1, "SD"),
        STANDARD(2, "HD"),
        PREMIUM(4, "UHD");

        private final int maxConcurrentStreams;
        private final String maxQuality;

        Plan(int maxConcurrentStreams, String maxQuality) {
            this.maxConcurrentStreams = maxConcurrentStreams;
            this.maxQuality = maxQuality;
        }

        public int maxConcurrentStreams() {
            return maxConcurrentStreams;
        }

        public String maxQuality() {
            return maxQuality;
        }
    }

    public enum Status {
        ACTIVE,
        PAUSED,
        CANCELLED
    }

    @Id
    private UUID id;

    @Column(name = "viewer_id", nullable = false, unique = true)
    private UUID viewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "renews_on")
    private LocalDate renewsOn;

    protected Subscription() {
        // voor JPA
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean allowsAnotherStream(int activeStreams) {
        return activeStreams < plan.maxConcurrentStreams();
    }

    public UUID id() {
        return id;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public Plan plan() {
        return plan;
    }

    public Status status() {
        return status;
    }

    public LocalDate startedOn() {
        return startedOn;
    }

    public LocalDate renewsOn() {
        return renewsOn;
    }
}
