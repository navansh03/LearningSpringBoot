package com.navansh.LearningSpringBoot.repository;

import com.navansh.LearningSpringBoot.entity.User;

import java.util.Optional;

public interface UserRepository {
    // Create
    User save(User user);

    // Read
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);

    // Check existence
    boolean existsByUsername(String username);
}