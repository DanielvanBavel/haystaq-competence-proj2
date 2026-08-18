package nl.haystaq.streamforge.entitlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.UUID;

/** Aggregate root: de kijker met zijn profielinstellingen. */
@Entity
@Table(name = "viewer")
public class Viewer {

    @Id
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    /** De regio waar de kijker zich bevindt, ISO 3166-1 alpha-2. */
    @Column(name = "region", nullable = false)
    private String region;

    /** Hoogste leeftijdsclassificatie die dit profiel mag zien. */
    @Column(name = "maturity_limit", nullable = false)
    private int maturityLimit;

    protected Viewer() {
        // voor JPA
    }

    public boolean mayWatch(int maturityRating) {
        return maturityRating <= maturityLimit;
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public String region() {
        return region == null ? null : region.toUpperCase(Locale.ROOT);
    }

    public int maturityLimit() {
        return maturityLimit;
    }
}
