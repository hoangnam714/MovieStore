package com.btlon.movie.store.dto;

import java.util.List;

public record MovieListResponse(List<MovieResponse> movies, long featuredCount) {
}
