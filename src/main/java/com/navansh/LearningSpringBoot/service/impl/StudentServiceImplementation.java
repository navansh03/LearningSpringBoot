package com.navansh.LearningSpringBoot.service.impl;

import com.navansh.LearningSpringBoot.dao.StudentDAO;
import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.exception.BadRequestException;
import com.navansh.LearningSpringBoot.exception.DuplicateResourceException;
import com.navansh.LearningSpringBoot.exception.ResourceNotFoundException;
import com.navansh.LearningSpringBoot.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImplementation implements StudentService {
    private final StudentDAO studentDAO;
    private final ModelMapper modelMapper;

    @Override
    public List<StudentDTO> getAllStudents(){
        List<Student> students = studentDAO.findAll();
        return students
                .stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));
        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public StudentDTO creatNewStudent(AddStudentDTO addStudentDTO) {
        // Check if email already exists
        if (studentDAO.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        Student newStudent = modelMapper.map(addStudentDTO, Student.class);
        Long id = studentDAO.save(newStudent);
        newStudent.setId(id);
        log.info("Student created successfully with ID: {}", id);
        return modelMapper.map(newStudent, StudentDTO.class);
    }

    @Override
    public void deleteStudentById(Long id) {
        if (!studentDAO.existsById(id)) {
            throw new ResourceNotFoundException("Student with id: " + id + " does not exist");
        }
        studentDAO.deleteById(id);
        log.info("Student with ID: {} deleted successfully", id);
    }

    @Override
    public StudentDTO updateStudentById(Long id, AddStudentDTO addStudentDTO) {
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));

        // Check if new email is already taken by another student
        if (!student.getEmail().equals(addStudentDTO.getEmail()) &&
                studentDAO.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        student.setName(addStudentDTO.getName());
        student.setEmail(addStudentDTO.getEmail());
        studentDAO.update(student);
        log.info("Student with ID: {} updated successfully", id);
        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public StudentDTO updatePartialStudent(Long id, Map<String, Object> updates) {
        Student student = studentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));

        Student finalStudent = student;
        updates.forEach((field, value) -> {
            switch (field) {
                case "name":
                    finalStudent.setName((String) value);
                    break;
                case "email":
                    String newEmail = (String) value;
                    // Check if new email is already taken
                    if (!finalStudent.getEmail().equals(newEmail) &&
                            studentDAO.findByEmail(newEmail).isPresent()) {
                        throw new DuplicateResourceException("Email: " + newEmail + " is already registered");
                    }
                    finalStudent.setEmail(newEmail);
                    break;
                default:
                    throw new BadRequestException("Field '" + field + "' is not supported for update");
            }
        });

        studentDAO.update(student);
        log.info("Student with ID: {} partially updated successfully", id);
        return modelMapper.map(student, StudentDTO.class);
    }
}