package com.navansh.LearningSpringBoot.service;

import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;

import java.util.List;
import java.util.Map;

public interface StudentService {
    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    StudentDTO creatNewStudent(AddStudentDTO addStudentDTO);

    void deleteStudentById(Long id);

    StudentDTO updateStudentById(Long id, AddStudentDTO addStudentDTO);

    StudentDTO updatePartialStudent(Long id, Map<String, Object> updates);
}
