package com.btlon.movie.store.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cinemas")
public class Cinema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(nullable = false, length = 255)
    private String address;

    public Cinema() {}
    public Cinema(String name, String address) { this.name = name; this.address = address; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
