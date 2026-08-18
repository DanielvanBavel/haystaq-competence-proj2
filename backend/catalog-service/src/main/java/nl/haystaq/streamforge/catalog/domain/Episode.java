package nl.haystaq.streamforge.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Entiteit binnen het aggregate {@link Title}. */
@Entity
@Table(name = "episode")
public class Episode {

    public enum AssetStatus {
        /** Transcodering afgerond, manifest beschikbaar. */
        READY,
        /** Ingest gestart maar nooit afgerond. */
        PENDING,
        /** Ingest definitief mislukt. */
        FAILED
    }

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(name = "episode_number", nullable = false)
    private int episodeNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_status", nullable = false)
    private AssetStatus assetStatus;

    @Column(name = "manifest_url")
    private String manifestUrl;

    protected Episode() {
        // voor JPA
    }

    public boolean isPlayable() {
        return assetStatus == AssetStatus.READY && manifestUrl != null;
    }

    public UUID id() {
        return id;
    }

    public Title title() {
        return title;
    }

    public int seasonNumber() {
        return seasonNumber;
    }

    public int episodeNumber() {
        return episodeNumber;
    }

    public String name() {
        return name;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public AssetStatus assetStatus() {
        return assetStatus;
    }

    public String manifestUrl() {
        return manifestUrl;
    }
}
