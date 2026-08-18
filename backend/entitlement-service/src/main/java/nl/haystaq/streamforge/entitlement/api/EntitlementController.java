package nl.haystaq.streamforge.entitlement.api;

import nl.haystaq.streamforge.entitlement.application.EntitlementService;
import org.flywaydb.core.Flyway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/entitlements")
public class EntitlementController {

    private final EntitlementService entitlements;
    private final Flyway flyway;

    public EntitlementController(EntitlementService entitlements, Flyway flyway) {
        this.entitlements = entitlements;
        this.flyway = flyway;
    }

    @GetMapping("/viewers")
    public List<EntitlementService.ViewerView> viewers() {
        return entitlements.viewers();
    }

    @GetMapping("/viewers/{id}")
    public EntitlementService.ViewerView viewer(@PathVariable UUID id) {
        return entitlements.viewer(id);
    }

    @PostMapping("/check")
    public EntitlementService.EntitlementDecision check(
            @RequestBody EntitlementService.EntitlementRequest request) {
        return entitlements.check(request);
    }

    @PostMapping("/admin/reset")
    public Map<String, Object> reset() {
        flyway.clean();
        return Map.of("service", "entitlement", "migrations", flyway.migrate().migrationsExecuted);
    }
}
