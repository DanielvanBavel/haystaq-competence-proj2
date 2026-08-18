package nl.haystaq.streamforge.catalog.application;

import nl.haystaq.streamforge.catalog.domain.Episode;
import nl.haystaq.streamforge.catalog.domain.IngestJob;
import nl.haystaq.streamforge.catalog.domain.Title;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CatalogViews {

    private CatalogViews() {
    }

    public record TitleView(
            UUID id,
            String slug,
            String name,
            String synopsis,
            int releaseYear,
            String genre,
            int maturityRating,
            int popularity,
            Set<String> availableRegions,
            Long similarTitles,
            List<EpisodeView> episodes) {

        public static TitleView of(Title title, Long similarTitles, List<EpisodeView> episodes) {
            return new TitleView(title.id(), title.slug(), title.name(), title.synopsis(), title.releaseYear(),
                    title.genre(), title.maturityRating(), title.popularity(), title.availableRegions(),
                    similarTitles, episodes);
        }
    }

    public record EpisodeView(
            UUID id,
            UUID titleId,
            String titleName,
            int seasonNumber,
            int episodeNumber,
            String name,
            int durationSeconds,
            String assetStatus,
            String manifestUrl,
            boolean playable,
            Set<String> availableRegions,
            int maturityRating) {

        public static EpisodeView of(Episode episode) {
            return new EpisodeView(
                    episode.id(),
                    episode.title().id(),
                    episode.title().name(),
                    episode.seasonNumber(),
                    episode.episodeNumber(),
                    episode.name(),
                    episode.durationSeconds(),
                    episode.assetStatus().name(),
                    episode.manifestUrl(),
                    episode.isPlayable(),
                    episode.title().availableRegions(),
                    episode.title().maturityRating());
        }
    }

    public record IngestJobView(
            UUID id,
            UUID episodeId,
            String status,
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt,
            String worker,
            String errorMessage) {

        public static IngestJobView of(IngestJob job) {
            return new IngestJobView(job.id(), job.episodeId(), job.status(), job.startedAt(),
                    job.finishedAt(), job.worker(), job.errorMessage());
        }
    }
}
