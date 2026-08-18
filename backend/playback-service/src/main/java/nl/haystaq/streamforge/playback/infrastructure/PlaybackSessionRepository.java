package nl.haystaq.streamforge.playback.infrastructure;

import nl.haystaq.streamforge.playback.domain.PlaybackSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaybackSessionRepository extends JpaRepository<PlaybackSession, UUID> {

    /**
     * Telt de sessies die volgens de administratie nog lopen. Sessies waarvan de
     * client nooit netjes is afgesloten staan hier ook nog bij.
     */
    int countByViewerIdAndStatus(UUID viewerId, PlaybackSession.Status status);

    List<PlaybackSession> findByViewerIdOrderByStartedAtDesc(UUID viewerId);

    List<PlaybackSession> findByStatusOrderByStartedAtDesc(PlaybackSession.Status status);
}
