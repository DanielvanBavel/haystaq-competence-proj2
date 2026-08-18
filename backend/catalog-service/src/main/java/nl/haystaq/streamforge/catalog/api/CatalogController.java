package nl.haystaq.streamforge.catalog.api;

import nl.haystaq.streamforge.catalog.application.CatalogService;
import nl.haystaq.streamforge.catalog.application.CatalogViews;
import org.flywaydb.core.Flyway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalog;
    private final Flyway flyway;

    public CatalogController(CatalogService catalog, Flyway flyway) {
        this.catalog = catalog;
        this.flyway = flyway;
    }

    @GetMapping("/titles")
    public List<CatalogViews.TitleView> titles(@RequestParam(required = false) String query) {
        return catalog.search(query);
    }

    @GetMapping("/titles/{slug}")
    public CatalogViews.TitleView title(@PathVariable String slug) {
        return catalog.bySlug(slug);
    }

    @GetMapping("/episodes/{id}")
    public CatalogViews.EpisodeView episode(@PathVariable UUID id) {
        return catalog.episode(id);
    }

    @GetMapping("/ingest-jobs")
    public List<CatalogViews.IngestJobView> ingestJobs(@RequestParam(required = false) UUID episodeId) {
        return episodeId == null ? catalog.recentIngestJobs() : catalog.ingestJobsFor(episodeId);
    }

    @PostMapping("/admin/reset")
    public Map<String, Object> reset() {
        flyway.clean();
        return Map.of("service", "catalog", "migrations", flyway.migrate().migrationsExecuted);
    }
}
