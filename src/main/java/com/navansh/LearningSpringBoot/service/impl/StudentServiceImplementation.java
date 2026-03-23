package com.navansh.LearningSpringBoot.service.impl;

import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.exception.BadRequestException;
import com.navansh.LearningSpringBoot.exception.DuplicateResourceException;
import com.navansh.LearningSpringBoot.exception.ResourceNotFoundException;
import com.navansh.LearningSpringBoot.repository.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<StudentDTO> getAllStudents(){
        List<Student> students = studentRepository.findAll();
        return students
                .stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));
        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public StudentDTO creatNewStudent(AddStudentDTO addStudentDTO) {
        // Check if email already exists
        if (studentRepository.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        Student newStudent = modelMapper.map(addStudentDTO, Student.class);
        Student student = studentRepository.save(newStudent);
        log.info("Student created successfully with ID: {}", student.getId());
        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    public void deleteStudentById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with id: " + id + " does not exist");
        }
        studentRepository.deleteById(id);
        log.info("Student with ID: {} deleted successfully", id);
    }

    @Override
    public StudentDTO updateStudentById(Long id, AddStudentDTO addStudentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id: " + id + " does not exist"));

        // Check if new email is already taken by another student
        if (!student.getEmail().equals(addStudentDTO.getEmail()) &&
                studentRepository.findByEmail(addStudentDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email: " + addStudentDTO.getEmail() + " is already registered");
        }

        modelMapper.map(addStudentDTO, student);
        student = studentRepository.save(student);
        log.info("Student with ID: {} updated successfully", id);
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
                    // Check if new email is already taken
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

        student = studentRepository.save(student);
        log.info("Student with ID: {} partially updated successfully", id);
        return modelMapper.map(student, StudentDTO.class);
    }
}