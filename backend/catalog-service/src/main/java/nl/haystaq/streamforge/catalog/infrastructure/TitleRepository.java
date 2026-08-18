package nl.haystaq.streamforge.catalog.infrastructure;

import nl.haystaq.streamforge.catalog.domain.Episode;
import nl.haystaq.streamforge.catalog.domain.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TitleRepository extends JpaRepository<Title, UUID> {

    Optional<Title> findBySlug(String slug);

    List<Title> findTop30ByOrderByPopularityDesc();

    @Query("select t from Title t where lower(t.name) like lower(concat('%', :term, '%'))")
    List<Title> searchByName(String term);

    @Query("select e from Episode e where e.id = :episodeId")
    Optional<Episode> findEpisode(UUID episodeId);

    @Query("select e from Episode e where e.title.id = :titleId order by e.seasonNumber, e.episodeNumber")
    List<Episode> findEpisodesOfTitle(UUID titleId);

    /**
     * Wordt gebruikt door de "meer zoals dit"-verrijking. Zie
     * {@code CatalogService#search}: deze query wordt per gevonden titel opnieuw
     * uitgevoerd.
     */
    @Query("select count(t) from Title t where t.genre = :genre and t.releaseYear between :from and :to")
    long countSimilar(String genre, int from, int to);

    /** Alle titels als tuple (id, genre, jaar), zonder de entiteiten te laden. */
    @Query("select t.id, t.genre, t.releaseYear from Title t")
    List<Object[]> allGenreAndYear();
}
