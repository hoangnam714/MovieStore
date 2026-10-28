package com.btlon.movie.store.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "genre", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonManagedReference
    @OrderBy("title ASC")
    private List<Movie> movies = new ArrayList<>();

    public MovieGenre() {
    }

    public MovieGenre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
        movie.setGenre(this);
    }

    /**
     * Phim bắt buộc có thể loại; chỉ gỡ khỏi danh sách trong bộ nhớ (không xóa
     * phim, không null genre).
     */
    public void removeMovie(Movie movie) {
        movies.remove(movie);
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
