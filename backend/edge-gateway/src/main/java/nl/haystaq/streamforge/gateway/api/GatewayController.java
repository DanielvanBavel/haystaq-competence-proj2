package nl.haystaq.streamforge.gateway.api;

import nl.haystaq.streamforge.common.DownstreamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Backend for frontend. De UI en de tests praten alleen met deze service; de
 * gateway verdeelt het werk over de drie backends.
 */
@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final DownstreamClient catalog;
    private final DownstreamClient playback;
    private final DownstreamClient entitlement;

    public GatewayController(@Qualifier("catalogClient") DownstreamClient catalog,
                             @Qualifier("playbackClient") DownstreamClient playback,
                             @Qualifier("entitlementClient") DownstreamClient entitlement) {
        this.catalog = catalog;
        this.playback = playback;
        this.entitlement = entitlement;
    }

    @GetMapping("/browse")
    public List<?> browse() {
        return catalog.get("/api/catalog/titles", List.class);
    }

    @GetMapping("/search")
    public List<?> search(@RequestParam String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        log.info("search request for '{}'", query);
        return catalog.get("/api/catalog/titles?query=" + encoded, List.class);
    }

    @GetMapping("/titles/{slug}")
    public Map<?, ?> title(@PathVariable String slug) {
        return catalog.get("/api/catalog/titles/" + slug, Map.class);
    }

    @GetMapping("/episodes/{id}")
    public Map<?, ?> episode(@PathVariable UUID id) {
        return catalog.get("/api/catalog/episodes/" + id, Map.class);
    }

    @GetMapping("/viewers")
    public List<?> viewers() {
        return entitlement.get("/api/entitlements/viewers", List.class);
    }

    @PostMapping("/play")
    public ResponseEntity<Map<?, ?>> play(@RequestBody Map<String, Object> request) {
        Map<?, ?> session = playback.post("/api/playback/sessions", request, Map.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/sessions/{id}/heartbeat")
    public Map<?, ?> heartbeat(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        return playback.post("/api/playback/sessions/" + id + "/heartbeat",
                body == null ? Map.of("positionSeconds", 0) : body, Map.class);
    }

    @PostMapping("/sessions/{id}/stop")
    public Map<?, ?> stop(@PathVariable UUID id) {
        return playback.post("/api/playback/sessions/" + id + "/stop", Map.of(), Map.class);
    }

    @GetMapping("/sessions")
    public List<?> sessions(@RequestParam(required = false) UUID viewerId) {
        String uri = viewerId == null ? "/api/playback/sessions" : "/api/playback/sessions?viewerId=" + viewerId;
        return playback.get(uri, List.class);
    }

    @GetMapping("/ingest-jobs")
    public List<?> ingestJobs(@RequestParam(required = false) UUID episodeId) {
        String uri = episodeId == null
                ? "/api/catalog/ingest-jobs"
                : "/api/catalog/ingest-jobs?episodeId=" + episodeId;
        return catalog.get(uri, List.class);
    }

    /** Handig bij het analyseren van een storing: wie doet het nog wel? */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("catalog", probe(catalog));
        result.put("entitlement", probe(entitlement));
        result.put("playback", probe(playback));
        return result;
    }

    private Object probe(DownstreamClient client) {
        try {
            return client.get("/actuator/health", Map.class);
        } catch (RuntimeException exception) {
            return Map.of("status", "DOWN", "error", String.valueOf(exception.getMessage()));
        }
    }

    @PostMapping("/admin/reset")
    public Map<String, Object> reset() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("catalog", catalog.post("/api/catalog/admin/reset", Map.of(), Map.class));
        result.put("entitlement", entitlement.post("/api/entitlements/admin/reset", Map.of(), Map.class));
        result.put("playback", playback.post("/api/playback/admin/reset", Map.of(), Map.class));
        return result;
    }
}
