package nl.haystaq.streamforge.playback.api;

import nl.haystaq.streamforge.playback.application.PlaybackService;
import org.flywaydb.core.Flyway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/playback")
public class PlaybackController {

    private final PlaybackService playback;
    private final Flyway flyway;

    public PlaybackController(PlaybackService playback, Flyway flyway) {
        this.playback = playback;
        this.flyway = flyway;
    }

    @PostMapping("/sessions")
    public ResponseEntity<PlaybackService.SessionView> start(@RequestBody PlaybackService.StartPlayback command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playback.start(command));
    }

    @PostMapping("/sessions/{id}/heartbeat")
    public PlaybackService.SessionView heartbeat(@PathVariable UUID id,
                                                 @RequestBody(required = false) PlaybackService.Heartbeat command) {
        return playback.heartbeat(id, command);
    }

    @PostMapping("/sessions/{id}/stop")
    public PlaybackService.SessionView stop(@PathVariable UUID id) {
        return playback.stop(id);
    }

    @GetMapping("/sessions")
    public List<PlaybackService.SessionView> sessions(@RequestParam(required = false) UUID viewerId) {
        return viewerId == null ? playback.active() : playback.byViewer(viewerId);
    }

    @PostMapping("/admin/reset")
    public Map<String, Object> reset() {
        flyway.clean();
        return Map.of("service", "playback", "migrations", flyway.migrate().migrationsExecuted);
    }
}
