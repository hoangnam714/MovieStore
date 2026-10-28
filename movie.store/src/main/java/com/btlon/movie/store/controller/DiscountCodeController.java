package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.DiscountCodeRequest;
import com.btlon.movie.store.entity.DiscountCode;
import com.btlon.movie.store.service.DiscountCodeService;
import com.btlon.movie.store.security.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/discount-codes")
@AdminOnly
public class DiscountCodeController {
    private final DiscountCodeService service;

    public DiscountCodeController(DiscountCodeService service) {
        this.service = service;
    }

    @GetMapping
    public List<DiscountCode> list() {
        return service.list();
    }

    @PostMapping
    public ResponseEntity<DiscountCode> create(@RequestBody DiscountCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountCode> update(@PathVariable Long id, @RequestBody DiscountCodeRequest request) {
        return service.update(id, request).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
