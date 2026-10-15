package com.example.demojpa.service;




import org.springframework.stereotype.Service;

import com.example.demojpa.dto.StudentRequest;
import com.example.demojpa.entity.Faculty;
import com.example.demojpa.entity.Student;
import com.example.demojpa.repository.FacultyRepository;
import com.example.demojpa.repository.StudentRepository;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
    }

    public Student createStudent(StudentRequest request) {
        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found with ID: " + request.getFacultyId()));

        Student student = new Student(request.getName(), request.getEmail(), request.getAge());
        student.setFaculty(faculty);

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}