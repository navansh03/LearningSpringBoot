package com.navansh.LearningSpringBoot.service;
public interface CacheService {

    //Invalidate cache for a specific student by Id
    void invalidateStudentCache(Long studentId);
     // Invalidate cache for all students list Called after creating, updating, or deleting any student
    void invalidateAllStudentsCache();
     // return TTL in minutes (e.g., 30)
    long getCacheTTLMinutes();

//    void invalidateAllStudentCache();
    // delete all caches
}