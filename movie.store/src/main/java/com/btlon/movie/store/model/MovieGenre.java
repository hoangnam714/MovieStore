package com.btlon.movie.store.model;

import java.util.ArrayList;
import java.util.List;

public class MovieGenre {
    private Long id;
    private String name;
    private List<Movie> movies = new ArrayList<>();

    public MovieGenre() {
    }

    public MovieGenre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Movie> getMovies() { return movies; }
    public void setMovies(List<Movie> movies) { this.movies = movies; }
}
