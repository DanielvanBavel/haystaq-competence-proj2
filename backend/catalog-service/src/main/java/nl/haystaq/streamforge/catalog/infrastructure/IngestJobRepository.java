package nl.haystaq.streamforge.catalog.infrastructure;

import nl.haystaq.streamforge.catalog.domain.IngestJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngestJobRepository extends JpaRepository<IngestJob, UUID> {

    List<IngestJob> findByEpisodeIdOrderByStartedAtDesc(UUID episodeId);

    List<IngestJob> findTop50ByOrderByStartedAtDesc();
}
