package com.example.demojpa.service;


import org.springframework.stereotype.Service;

import com.example.demojpa.entity.Faculty;
import com.example.demojpa.repository.FacultyRepository;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public Faculty getFacultyById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Faculty not found with ID: " + id));
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }
}