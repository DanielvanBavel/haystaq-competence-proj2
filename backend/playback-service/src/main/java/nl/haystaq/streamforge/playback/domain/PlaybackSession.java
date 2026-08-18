package nl.haystaq.streamforge.playback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Aggregate root: één kijksessie van één kijker op één apparaat. */
@Entity
@Table(name = "playback_session")
public class PlaybackSession {

    public enum Status {
        ACTIVE,
        ENDED
    }

    @Id
    private UUID id;

    @Column(name = "viewer_id", nullable = false)
    private UUID viewerId;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * Oudere clients sturen dit veld niet mee. De kolom is daarom nullable.
     */
    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "quality", nullable = false)
    private String quality;

    @Column(name = "manifest_url", nullable = false)
    private String manifestUrl;

    @Column(name = "position_seconds", nullable = false)
    private int positionSeconds;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "last_heartbeat_at", nullable = false)
    private OffsetDateTime lastHeartbeatAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    protected PlaybackSession() {
        // voor JPA
    }

    public static PlaybackSession start(UUID viewerId, UUID episodeId, String deviceType, String quality,
                                        String manifestUrl) {
        PlaybackSession session = new PlaybackSession();
        session.id = UUID.randomUUID();
        session.viewerId = viewerId;
        session.episodeId = episodeId;
        session.status = Status.ACTIVE;
        session.deviceType = deviceType;
        session.quality = quality;
        session.manifestUrl = manifestUrl;
        session.positionSeconds = 0;
        session.startedAt = OffsetDateTime.now();
        session.lastHeartbeatAt = OffsetDateTime.now();
        return session;
    }

    public void heartbeat(int positionSeconds) {
        this.positionSeconds = positionSeconds;
        this.lastHeartbeatAt = OffsetDateTime.now();
    }

    public void end() {
        this.status = Status.ENDED;
        this.endedAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public UUID episodeId() {
        return episodeId;
    }

    public Status status() {
        return status;
    }

    public String deviceType() {
        return deviceType;
    }

    public String quality() {
        return quality;
    }

    public String manifestUrl() {
        return manifestUrl;
    }

    public int positionSeconds() {
        return positionSeconds;
    }

    public OffsetDateTime startedAt() {
        return startedAt;
    }

    public OffsetDateTime lastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public OffsetDateTime endedAt() {
        return endedAt;
    }
}
