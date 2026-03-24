package com.navansh.LearningSpringBoot.dao;

import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.mapper.StudentRowMapper;
import com.navansh.LearningSpringBoot.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.persistence.enableJPA",
        havingValue = "false",
        matchIfMissing = true
)
public class StudentDAO implements StudentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final StudentRowMapper studentRowMapper;

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO student (name, email) VALUES (?, ?)";
        jdbcTemplate.update(sql, student.getName(), student.getEmail());

        String idSql = "SELECT id FROM student WHERE email = ? ORDER BY id DESC LIMIT 1";
        Long id = jdbcTemplate.queryForObject(idSql, Long.class, student.getEmail());
        student.setId(id);
        return student;
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT id, name, email FROM student";
        return jdbcTemplate.query(sql, studentRowMapper);
    }

    @Override
    public Optional<Student> findById(Long id) {
        String sql = "SELECT id, name, email FROM student WHERE id = ?";
        try {
            Student student = jdbcTemplate.queryForObject(sql,
                    new Object[]{id},
                    studentRowMapper);
            return Optional.of(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT id, name, email FROM student WHERE email = ?";
        try {
            Student student = jdbcTemplate.queryForObject(sql,
                    new Object[]{email},
                    studentRowMapper);
            return Optional.of(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE student SET name = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, student.getName(), student.getEmail(), student.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM student WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM student WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }
}