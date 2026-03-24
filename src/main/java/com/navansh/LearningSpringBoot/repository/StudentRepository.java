package com.navansh.LearningSpringBoot.repository;

import com.navansh.LearningSpringBoot.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    // Create
    Student save(Student student);

    // Read
    List<Student> findAll();
    Optional<Student> findById(Long id);
    Optional<Student> findByEmail(String email);

    // Update
    void update(Student student);

    // Delete
    void deleteById(Long id);

    // Check existence
    boolean existsById(Long id);
}