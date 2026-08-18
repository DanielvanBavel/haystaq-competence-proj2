package nl.haystaq.streamforge.catalog.application;

import nl.haystaq.streamforge.catalog.domain.Episode;
import nl.haystaq.streamforge.catalog.domain.Title;
import nl.haystaq.streamforge.catalog.infrastructure.IngestJobRepository;
import nl.haystaq.streamforge.catalog.infrastructure.TitleRepository;
import nl.haystaq.streamforge.common.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    /** Vanaf drie tekens draait de aanbevelingsverrijking mee (release 4.2). */
    private static final int ENRICHMENT_THRESHOLD = 3;

    private final TitleRepository titles;
    private final IngestJobRepository ingestJobs;

    public CatalogService(TitleRepository titles, IngestJobRepository ingestJobs) {
        this.titles = titles;
        this.ingestJobs = ingestJobs;
    }

    public List<CatalogViews.TitleView> search(String query) {
        List<Title> matches = query == null || query.isBlank()
                ? titles.findTop30ByOrderByPopularityDesc()
                : titles.searchByName(query.trim());
        log.debug("search query='{}' matches={}", query, matches.size());

        if (query != null && query.trim().length() >= ENRICHMENT_THRESHOLD) {
            Map<UUID, Long> similar = similarTitleCounts();
            return matches.stream()
                    .map(title -> CatalogViews.TitleView.of(title, similar.get(title.id()), List.of()))
                    .toList();
        }
        return matches.stream()
                .map(title -> CatalogViews.TitleView.of(title, null, List.of()))
                .toList();
    }

    /**
     * Berekent voor elke titel in de catalogus hoeveel vergelijkbare titels er
     * zijn, zodat de zoekresultaten een "meer zoals dit"-teller kunnen tonen.
     */
    private Map<UUID, Long> similarTitleCounts() {
        long started = System.nanoTime();
        List<Object[]> everything = titles.allGenreAndYear();
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : everything) {
            UUID id = (UUID) row[0];
            String genre = (String) row[1];
            int year = (Integer) row[2];
            counts.put(id, titles.countSimilar(genre, year - 3, year + 3));
        }
        long millis = (System.nanoTime() - started) / 1_000_000;
        log.debug("enrichment: {} titles scanned, {} queries, {}ms", everything.size(), everything.size() + 1, millis);
        if (millis > 1000) {
            log.warn("enrichment took {}ms for {} titles", millis, everything.size());
        }
        return counts;
    }

    public CatalogViews.TitleView bySlug(String slug) {
        Title title = titles.findBySlug(slug)
                .orElseThrow(() -> ServiceException.notFound("title_not_found", "no title with slug " + slug));
        List<CatalogViews.EpisodeView> episodes = titles.findEpisodesOfTitle(title.id()).stream()
                .map(CatalogViews.EpisodeView::of)
                .toList();
        return CatalogViews.TitleView.of(title, null, episodes);
    }

    public CatalogViews.EpisodeView episode(UUID episodeId) {
        Episode episode = titles.findEpisode(episodeId)
                .orElseThrow(() -> ServiceException.notFound("episode_not_found", "no episode " + episodeId));
        if (!episode.isPlayable()) {
            log.warn("episode {} ({} S{}E{}) is not playable: status={} manifest={}",
                    episode.id(), episode.title().name(), episode.seasonNumber(), episode.episodeNumber(),
                    episode.assetStatus(), episode.manifestUrl());
        }
        return CatalogViews.EpisodeView.of(episode);
    }

    public List<CatalogViews.IngestJobView> recentIngestJobs() {
        return ingestJobs.findTop50ByOrderByStartedAtDesc().stream()
                .map(CatalogViews.IngestJobView::of)
                .toList();
    }

    public List<CatalogViews.IngestJobView> ingestJobsFor(UUID episodeId) {
        return ingestJobs.findByEpisodeIdOrderByStartedAtDesc(episodeId).stream()
                .map(CatalogViews.IngestJobView::of)
                .toList();
    }
}
