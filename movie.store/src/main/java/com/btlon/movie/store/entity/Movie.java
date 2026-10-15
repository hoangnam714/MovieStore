package com.btlon.movie.store.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    @JsonBackReference
    private MovieGenre genre;

    public Movie() {
    }

    public Movie(Long id, String title, Integer duration, Integer releaseYear, MovieGenre genre) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public MovieGenre getGenre() { return genre; }
    public void setGenre(MovieGenre genre) { this.genre = genre; }

    // Preserve the genreId field used by the frontend without mapping the FK twice.
    public Long getGenreId() { return genre == null ? null : genre.getId(); }
}
