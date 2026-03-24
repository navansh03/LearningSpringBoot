package com.navansh.LearningSpringBoot.repository;

import com.navansh.LearningSpringBoot.entity.Student;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.navansh.LearningSpringBoot.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(
        name = "app.persistence.enableJPA",
        havingValue = "true",
        matchIfMissing = false
)
public interface JpaStudentRepository extends JpaRepository<Student, Long>, StudentRepository {

    @Override
    Optional<Student> findByEmail(String email);

    @Override
    default Student save(Student student) {
        return ((JpaRepository<Student, Long>) this).save(student);
    }
    @Override
    default void update(Student student) {
        save(student);
    }
}