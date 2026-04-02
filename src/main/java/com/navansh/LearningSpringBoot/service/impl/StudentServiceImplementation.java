package com.navansh.LearningSpringBoot.service.impl;

import com.navansh.LearningSpringBoot.config.CacheConfig;
import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.exception.BadRequestException;
import com.navansh.LearningSpringBoot.exception.DuplicateResourceException;
import com.navansh.LearningSpringBoot.exception.ResourceNotFoundException;
import com.navansh.LearningSpringBoot.repository.StudentRepository;
import com.navansh.LearningSpringBoot.service.CacheService;
import com.navansh.LearningSpringBoot.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImplementation implements StudentService {
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    //if anything is null it will be handled by object hanler
    private final ObjectProvider<CacheService> cacheServiceProvider;

    private CacheService getCacheService() {
        return cacheServiceProvider.getIfAvailable();
    }

    @Override
    @Cacheable(
            value = CacheConfig.ALL_STUDENTS_CACHE,
            unless = "#result.isEmpty()"
    )
    public List<StudentDTO> getAllStudents(){
        log.info("Cache miss fetching all students");
        List<Student> students = studentRepository.findAll();
        return students
                .stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    @Override
    @Cacheable(
            value = CacheConfig.STUDENT_CACHE,
            key = "#id"
    )
    public StudentDTO getStudentById(Long id) {
        log.info("Cache miss fetching students by id: "+id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));
        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public StudentDTO creatNewStudent(AddStudentDTO addStudentDTO) {
        if (studentRepository.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        Student newStudent = modelMapper.map(addStudentDTO, Student.class);
        Student savedStudent = studentRepository.save(newStudent);
        log.info("Student created successfully with ID: {}", savedStudent.getId());

        CacheService cacheService = getCacheService();
        if (cacheService != null) {
            cacheService.invalidateAllStudentsCache();
        }

        return modelMapper.map(savedStudent, StudentDTO.class);
    }

    @Override
    public void deleteStudentById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with id: " + id + " does not exist");
        }
        studentRepository.deleteById(id);
        log.info("Student with ID: {} deleted successfully", id);

        CacheService cacheService = getCacheService();
        if (cacheService != null) {
            cacheService.invalidateAllStudentsCache();
        }
    }

    @Override
    public StudentDTO updateStudentById(Long id, AddStudentDTO addStudentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));

        if (!student.getEmail().equals(addStudentDTO.getEmail()) &&
                studentRepository.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        student.setName(addStudentDTO.getName());
        student.setEmail(addStudentDTO.getEmail());
        studentRepository.update(student);
        log.info("Student with ID: {} updated successfully", id);

        CacheService cacheService = getCacheService();
        if (cacheService != null) {
            cacheService.invalidateAllStudentsCache();
        }

        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public StudentDTO updatePartialStudent(Long id, Map<String, Object> updates) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));

        Student finalStudent = student;
        updates.forEach((field, value) -> {
            switch (field) {
                case "name":
                    finalStudent.setName((String) value);
                    break;
                case "email":
                    String newEmail = (String) value;
                    if (!finalStudent.getEmail().equals(newEmail) &&
                            studentRepository.findByEmail(newEmail).isPresent()) {
                        throw new DuplicateResourceException("Email: " + newEmail + " is already registered");
                    }
                    finalStudent.setEmail(newEmail);
                    break;
                default:
                    throw new BadRequestException("Field '" + field + "' is not supported for update");
            }
        });

        studentRepository.update(student);
        log.info("Student with ID: {} partially updated successfully", id);

        CacheService cacheService = getCacheService();
        if (cacheService != null) {
            cacheService.invalidateAllStudentsCache();
        }

        return modelMapper.map(student, StudentDTO.class);
    }
}
