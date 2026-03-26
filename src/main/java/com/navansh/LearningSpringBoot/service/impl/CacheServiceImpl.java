package com.navansh.LearningSpringBoot.service.impl;

import com.navansh.LearningSpringBoot.config.CacheConfig;
import com.navansh.LearningSpringBoot.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    @Value("${app.cache.ttl.minutes:30}")
    private long cacheTTLMinutes;
    @Override
    public void invalidateStudentCache(Long studentId) {
        if (cacheManager == null) {
            log.warn("CacheManager is null, skipping student cache invalidation");
            return;
        }
        org.springframework.cache.Cache cache = cacheManager.getCache(CacheConfig.STUDENT_CACHE);
        if (cache != null) {
            cache.evict(studentId);
            log.info("Invalidated cache for student ID: {}", studentId);
        } else {
            log.warn("Cache '{}' not found in CacheManager", CacheConfig.STUDENT_CACHE);
        }
    }
//    Clears the List<StudentDTO> from the cache
    @Override
    public void invalidateAllStudentsCache() {
        if (cacheManager == null) {
            log.warn("CacheManager is null, skipping allStudents cache invalidation");
            return;
        }

        org.springframework.cache.Cache cache = cacheManager.getCache(CacheConfig.ALL_STUDENTS_CACHE);
        if (cache != null) {
            cache.clear();
            log.info("Invalidated cache for all students");
        } else {
            log.warn("Cache '{}' not found in CacheManager", CacheConfig.ALL_STUDENTS_CACHE);
        }
    }
    //this will return the remaining time of ttl
    @Override
    public long getCacheTTLMinutes() {
        return cacheTTLMinutes;
    }

}