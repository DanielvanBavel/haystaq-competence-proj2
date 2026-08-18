package nl.haystaq.streamforge.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Administratie van de transcodeerjobs. Als een aflevering niet afspeelt, staat
 * hier meestal waarom.
 */
@Entity
@Table(name = "ingest_job")
public class IngestJob {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "worker", nullable = false)
    private String worker;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    protected IngestJob() {
        // voor JPA
    }

    public UUID id() {
        return id;
    }

    public UUID episodeId() {
        return episodeId;
    }

    public String status() {
        return status;
    }

    public OffsetDateTime startedAt() {
        return startedAt;
    }

    public OffsetDateTime finishedAt() {
        return finishedAt;
    }

    public String worker() {
        return worker;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
