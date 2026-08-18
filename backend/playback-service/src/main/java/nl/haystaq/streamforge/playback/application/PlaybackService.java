package nl.haystaq.streamforge.playback.application;

import nl.haystaq.streamforge.common.DownstreamClient;
import nl.haystaq.streamforge.common.ServiceException;
import nl.haystaq.streamforge.playback.domain.PlaybackSession;
import nl.haystaq.streamforge.playback.infrastructure.PlaybackSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PlaybackService {

    private static final Logger log = LoggerFactory.getLogger(PlaybackService.class);

    private final PlaybackSessionRepository sessions;
    private final DownstreamClient catalog;
    private final DownstreamClient entitlement;

    public PlaybackService(PlaybackSessionRepository sessions,
                           @Qualifier("catalogClient") DownstreamClient catalog,
                           @Qualifier("entitlementClient") DownstreamClient entitlement) {
        this.sessions = sessions;
        this.catalog = catalog;
        this.entitlement = entitlement;
    }

    public record StartPlayback(UUID viewerId, UUID episodeId, String deviceType) {
    }

    public record Heartbeat(int positionSeconds) {
    }

    public record SessionView(UUID id, UUID viewerId, UUID episodeId, String status, String deviceType,
                              String quality, String manifestUrl, int positionSeconds,
                              String startedAt, String lastHeartbeatAt, String endedAt) {

        static SessionView of(PlaybackSession session) {
            return new SessionView(session.id(), session.viewerId(), session.episodeId(), session.status().name(),
                    session.deviceType(), session.quality(), session.manifestUrl(), session.positionSeconds(),
                    String.valueOf(session.startedAt()), String.valueOf(session.lastHeartbeatAt()),
                    session.endedAt() == null ? null : String.valueOf(session.endedAt()));
        }
    }

    @SuppressWarnings("unchecked")
    public SessionView start(StartPlayback command) {
        if (command.viewerId() == null || command.episodeId() == null) {
            throw ServiceException.badRequest("missing_parameters", "viewerId and episodeId are required");
        }

        Map<String, Object> episode = catalog.get("/api/catalog/episodes/" + command.episodeId(), Map.class);
        boolean playable = Boolean.TRUE.equals(episode.get("playable"));
        if (!playable) {
            log.warn("episode {} not playable: assetStatus={} manifestUrl={}",
                    command.episodeId(), episode.get("assetStatus"), episode.get("manifestUrl"));
            throw ServiceException.conflict("manifest_unavailable",
                    "no playable asset for episode " + command.episodeId());
        }

        int activeStreams = sessions.countByViewerIdAndStatus(command.viewerId(), PlaybackSession.Status.ACTIVE);
        log.debug("viewer {} currently has {} active sessions according to playback", command.viewerId(), activeStreams);

        Map<String, Object> decision = entitlement.post("/api/entitlements/check", Map.of(
                "viewerId", command.viewerId(),
                "episodeId", command.episodeId(),
                "maturityRating", episode.getOrDefault("maturityRating", 18),
                "availableRegions", episode.getOrDefault("availableRegions", Set.of()),
                "activeStreams", activeStreams
        ), Map.class);

        if (!Boolean.TRUE.equals(decision.get("allowed"))) {
            String reason = String.valueOf(decision.get("reason"));
            log.info("playback denied for viewer {}: {}", command.viewerId(), reason);
            throw switch (reason) {
                case "region_blocked", "maturity_blocked", "subscription_inactive" ->
                        ServiceException.forbidden(reason, "playback not allowed: " + reason);
                case "stream_limit_reached" ->
                        ServiceException.conflict(reason, "playback not allowed: " + reason);
                default -> ServiceException.forbidden("not_entitled", "playback not allowed");
            };
        }

        PlaybackSession session = PlaybackSession.start(
                command.viewerId(),
                command.episodeId(),
                command.deviceType(),
                String.valueOf(decision.get("maxQuality")),
                String.valueOf(episode.get("manifestUrl")));

        sessions.save(session);
        log.info("playback session {} started for viewer {} on episode {}",
                session.id(), command.viewerId(), command.episodeId());
        return SessionView.of(session);
    }

    public SessionView heartbeat(UUID sessionId, Heartbeat command) {
        PlaybackSession session = sessions.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("session_not_found", "no session " + sessionId));
        if (session.status() == PlaybackSession.Status.ENDED) {
            throw ServiceException.conflict("session_ended", "session " + sessionId + " has already ended");
        }

        // Sinds release 5.1 wordt per apparaattype een andere bufferstrategie gekozen.
        String deviceProfile = session.deviceType().toUpperCase(Locale.ROOT);
        log.debug("heartbeat for session {} on device profile {}", sessionId, deviceProfile);

        session.heartbeat(command == null ? session.positionSeconds() : command.positionSeconds());
        return SessionView.of(sessions.save(session));
    }

    public SessionView stop(UUID sessionId) {
        PlaybackSession session = sessions.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("session_not_found", "no session " + sessionId));
        session.end();
        log.info("playback session {} ended", sessionId);
        return SessionView.of(sessions.save(session));
    }

    @Transactional(readOnly = true)
    public List<SessionView> byViewer(UUID viewerId) {
        return sessions.findByViewerIdOrderByStartedAtDesc(viewerId).stream().map(SessionView::of).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionView> active() {
        return sessions.findByStatusOrderByStartedAtDesc(PlaybackSession.Status.ACTIVE).stream()
                .map(SessionView::of)
                .toList();
    }
}
