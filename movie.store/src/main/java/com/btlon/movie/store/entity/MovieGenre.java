package com.btlon.movie.store.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "movie_genres")
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "genres")
    @OrderBy("title ASC")
    private List<Movie> movies = new ArrayList<>();

    public MovieGenre() {
    }

    public MovieGenre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMovie(Movie movie) {
        if (!movies.contains(movie)) movies.add(movie);
        if (!movie.getGenres().contains(this)) movie.getGenres().add(this);
    }

    public void removeMovie(Movie movie) {
        movies.remove(movie);
        movie.getGenres().remove(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        List<Movie> replacements = movies == null ? List.of() : new ArrayList<>(movies);
        for (Movie movie : new ArrayList<>(this.movies)) {
            removeMovie(movie);
        }
        replacements.forEach(this::addMovie);
    }
}
