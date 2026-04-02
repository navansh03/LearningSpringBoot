package com.navansh.LearningSpringBoot.test;

import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.exception.BadRequestException;
import com.navansh.LearningSpringBoot.exception.DuplicateResourceException;
import com.navansh.LearningSpringBoot.exception.ResourceNotFoundException;
import com.navansh.LearningSpringBoot.repository.StudentRepository;
import com.navansh.LearningSpringBoot.service.CacheService;
import com.navansh.LearningSpringBoot.service.impl.StudentServiceImplementation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplementationTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ObjectProvider<CacheService> cacheServiceProvider;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private StudentServiceImplementation studentService;

    @Test
    void getStudentById_whenFound_returnsMappedDto() {
        Long id = 1L;
        Student student = new Student(id, "Navansh", "n@test.com");
        StudentDTO dto = new StudentDTO("Navansh", id, "n@test.com", null);

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(modelMapper.map(student, StudentDTO.class)).thenReturn(dto);

        StudentDTO result = studentService.getStudentById(id);

        assertEquals(id, result.getId());
        assertEquals("Navansh", result.getName());
        assertEquals("n@test.com", result.getEmail());
        verify(studentRepository).findById(id);
    }

    @Test
    void getStudentById_whenMissing_throwsResourceNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(99L));
        verify(studentRepository).findById(99L);
    }

    @Test
    void creatNewStudent_whenEmailExists_throwsDuplicateResourceException() {
        AddStudentDTO req = new AddStudentDTO();
        req.setName("Test User");
        req.setEmail("dup@test.com");

        when(studentRepository.findByEmail("dup@test.com"))
                .thenReturn(Optional.of(new Student(7L, "Old", "dup@test.com")));

        assertThrows(DuplicateResourceException.class, () -> studentService.creatNewStudent(req));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void creatNewStudent_whenValid_savesAndInvalidatesCache() {
        AddStudentDTO req = new AddStudentDTO();
        req.setName("New User");
        req.setEmail("new@test.com");

        Student mapped = new Student(null, "New User", "new@test.com");
        Student saved = new Student(10L, "New User", "new@test.com");
        StudentDTO dto = new StudentDTO("New User", 10L, "new@test.com", null);

        when(studentRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(cacheServiceProvider.getIfAvailable()).thenReturn(cacheService);
        when(modelMapper.map(req, Student.class)).thenReturn(mapped);
        when(studentRepository.save(mapped)).thenReturn(saved);
        when(modelMapper.map(saved, StudentDTO.class)).thenReturn(dto);

        StudentDTO result = studentService.creatNewStudent(req);

        assertEquals(10L, result.getId());
        verify(studentRepository).save(mapped);
        verify(cacheService).invalidateAllStudentsCache();
    }

    @Test
    void deleteStudentById_whenMissing_throwsResourceNotFound() {
        when(studentRepository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudentById(5L));
        verify(studentRepository, never()).deleteById(anyLong());
    }

    @Test
    void updatePartialStudent_whenUnsupportedField_throwsBadRequest() {
        Student existing = new Student(2L, "Old Name", "old@test.com");
        when(studentRepository.findById(2L)).thenReturn(Optional.of(existing));

        Map<String, Object> updates = Map.of("age", 22);

        assertThrows(BadRequestException.class, () -> studentService.updatePartialStudent(2L, updates));
        verify(studentRepository, never()).update(any());
    }

    @Test
    void getAllStudents_returnsMappedList() {
        Student s1 = new Student(1L, "A", "a@test.com");
        Student s2 = new Student(2L, "B", "b@test.com");

        when(studentRepository.findAll()).thenReturn(List.of(s1, s2));
        when(modelMapper.map(s1, StudentDTO.class)).thenReturn(new StudentDTO("A", 1L, "a@test.com", null));
        when(modelMapper.map(s2, StudentDTO.class)).thenReturn(new StudentDTO("B", 2L, "b@test.com", null));

        List<StudentDTO> result = studentService.getAllStudents();

        assertEquals(2, result.size());
        verify(studentRepository).findAll();
    }
}

