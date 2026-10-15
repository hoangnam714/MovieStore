package com.example.demojpa.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demojpa.entity.Faculty;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
}