package com.navansh.LearningSpringBoot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.time.Duration;
/**
 * Redis Cache Configuration
 * Enables Spring Cache abstraction with Redis backend
 * Configures automatic 30-minute TTL for all cached entries via application.properties.
*/
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {

    public static final String STUDENT_CACHE = "student"; //→ Cache key prefix for individual student by ID (e.g., "student::1")
    public static final String ALL_STUDENTS_CACHE = "allStudents"; //→ Cache key for list of all students

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("Initializing Redis CacheManager with TTL");
        RedisCacheConfiguration config=RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

}