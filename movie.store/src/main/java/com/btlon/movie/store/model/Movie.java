package com.btlon.movie.store.model;

public class Movie {
    private Long id;
    private String title;
    private Integer duration;
    private Integer releaseYear;
    private Long genreId;

    public Movie() {
    }

    public Movie(Long id, String title, Integer duration, Integer releaseYear, Long genreId) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.releaseYear = releaseYear;
        this.genreId = genreId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Long getGenreId() { return genreId; }
    public void setGenreId(Long genreId) { this.genreId = genreId; }
}
