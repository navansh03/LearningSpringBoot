package com.navansh.LearningSpringBoot.dao;

import com.navansh.LearningSpringBoot.entity.User;
import com.navansh.LearningSpringBoot.mapper.UserRowMapper;
import com.navansh.LearningSpringBoot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.persistence.enableJPA",
        havingValue = "false",
        matchIfMissing = true
)
public class UserDAO implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getRole());

        String idSql = "SELECT id FROM users WHERE username = ? ORDER BY id DESC LIMIT 1";
        Long id = jdbcTemplate.queryForObject(idSql, Long.class, user.getUsername());
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql,
                    new Object[]{username},
                    userRowMapper);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, password, role FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql,
                    new Object[]{id},
                    userRowMapper);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{username}, Integer.class);
        return count != null && count > 0;
    }
}