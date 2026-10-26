package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.RoomRequest;
import com.btlon.movie.store.entity.Room;
import com.btlon.movie.store.service.RoomService;
import com.btlon.movie.store.security.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return roomService.getRoomById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @AdminOnly
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        return roomService.updateRoom(id, request).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoom(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
