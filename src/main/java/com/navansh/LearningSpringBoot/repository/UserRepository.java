package com.navansh.LearningSpringBoot.repository;

import com.navansh.LearningSpringBoot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //herer for hte a custom query
    Optional<User> findByUsername(String username);
}

