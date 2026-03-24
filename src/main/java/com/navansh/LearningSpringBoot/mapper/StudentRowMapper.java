package com.navansh.LearningSpringBoot.mapper;

import com.navansh.LearningSpringBoot.entity.Student;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StudentRowMapper implements RowMapper<Student> {
    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            Student student = new Student();
            student.setId(rs.getLong("id"));
            student.setName(rs.getString("name"));
            student.setEmail(rs.getString("email"));
            return student;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}