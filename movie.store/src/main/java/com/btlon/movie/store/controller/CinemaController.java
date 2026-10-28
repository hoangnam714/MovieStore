package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.CinemaRequest;
import com.btlon.movie.store.entity.Cinema;
import com.btlon.movie.store.service.CinemaService;
import com.btlon.movie.store.security.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {
    private final CinemaService service;
    public CinemaController(CinemaService service) { this.service = service; }
    @GetMapping public List<Cinema> list() { return service.list(); }
    @AdminOnly @PostMapping public ResponseEntity<Cinema> create(@RequestBody CinemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @AdminOnly @PutMapping("/{id}") public ResponseEntity<Cinema> update(@PathVariable Long id, @RequestBody CinemaRequest request) {
        return service.update(id, request).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @AdminOnly @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
