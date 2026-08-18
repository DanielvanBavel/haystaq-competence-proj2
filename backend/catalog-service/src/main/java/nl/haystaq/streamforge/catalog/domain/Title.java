package nl.haystaq.streamforge.catalog.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Aggregate root van de catalogus: een serie of film met zijn afleveringen. */
@Entity
@Table(name = "title")
public class Title {

    @Id
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "synopsis")
    private String synopsis;

    @Column(name = "release_year", nullable = false)
    private int releaseYear;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "maturity_rating", nullable = false)
    private int maturityRating;

    @Column(name = "popularity", nullable = false)
    private int popularity;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "title_region", joinColumns = @JoinColumn(name = "title_id"))
    @Column(name = "region")
    private Set<String> availableRegions;

    @OneToMany(mappedBy = "title", fetch = FetchType.LAZY)
    @OrderBy("seasonNumber, episodeNumber")
    private List<Episode> episodes = new ArrayList<>();

    protected Title() {
        // voor JPA
    }

    /** Mag deze titel in deze regio getoond worden? */
    public boolean isAvailableIn(String region) {
        return region != null && availableRegions.contains(region.toUpperCase(Locale.ROOT));
    }

    public Optional<Episode> episode(int season, int number) {
        return episodes.stream()
                .filter(e -> e.seasonNumber() == season && e.episodeNumber() == number)
                .findFirst();
    }

    public UUID id() {
        return id;
    }

    public String slug() {
        return slug;
    }

    public String name() {
        return name;
    }

    public String synopsis() {
        return synopsis;
    }

    public int releaseYear() {
        return releaseYear;
    }

    public String genre() {
        return genre;
    }

    public int maturityRating() {
        return maturityRating;
    }

    public int popularity() {
        return popularity;
    }

    public Set<String> availableRegions() {
        return availableRegions;
    }

    public List<Episode> episodes() {
        return List.copyOf(episodes);
    }
}
