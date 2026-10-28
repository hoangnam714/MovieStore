package com.btlon.movie.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private Integer duration;

    // Preserve the original year until all legacy movies have an accurate release date.
    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "poster_url", length = 1000)
    private String posterUrl;

    @Column(length = 100)
    private String subtitle;

    @Column(length = 500)
    private String description;

    // NULL identifies pre-migration rows; new movies default to not featured.
    private Boolean featured = false;

    @Column(name = "banner_position")
    private Integer bannerPosition;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "movie_genre_links",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = { "movie_id", "genre_id" }))
    @OrderBy("name ASC, id ASC")
    @JsonIgnore
    private List<MovieGenre> genres = new ArrayList<>();

    public Movie() {
    }

    public Movie(Long id, String title, Integer duration, Integer releaseYear, MovieGenre genre) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.releaseYear = releaseYear;
        if (genre != null) this.genres.add(genre);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getReleaseYear() {
        return releaseDate == null ? releaseYear : releaseDate.getYear();
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        if (releaseDate != null) this.releaseYear = releaseDate.getYear();
    }

    public String getPosterUrl() {
        return posterUrl;
    }
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isFeatured() {
        return Boolean.TRUE.equals(featured);
    }
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    public Integer getBannerPosition() {
        return bannerPosition;
    }
    public void setBannerPosition(Integer bannerPosition) {
        this.bannerPosition = bannerPosition;
    }

    public List<MovieGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<MovieGenre> genres) {
        this.genres = new ArrayList<>(genres);
    }
}
