package com.navansh.LearningSpringBoot.repository;

import com.navansh.LearningSpringBoot.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.navansh.LearningSpringBoot.repository.UserRepository;
import java.util.Optional;

@Repository
@ConditionalOnProperty(
        name = "app.persistence.enableJPA",
        havingValue = "true",
        matchIfMissing = false
)
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    public final Logger logger= LoggerFactory.getLogger(JpaUserRepository.class);
    @Override
    Optional<User> findByUsername(String username);
    @Override
    default User save(User user) {
        return ((JpaRepository<User, Long>) this).save(user);
    }

    @Override
    default boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}