package com.btlon.movie.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    // Nullable while existing databases are upgraded; new rooms must select a cinema.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cinema_id")
    @JsonIgnore
    private Cinema cinema;

    public Room() {
    }

    public Room(String name, Integer seatCount) {
        this.name = name;
        this.seatCount = seatCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    public Cinema getCinema() { return cinema; }
    public void setCinema(Cinema cinema) { this.cinema = cinema; }
    public Long getCinemaId() { return cinema == null ? null : cinema.getId(); }
    public String getCinemaName() { return cinema == null ? null : cinema.getName(); }
}
