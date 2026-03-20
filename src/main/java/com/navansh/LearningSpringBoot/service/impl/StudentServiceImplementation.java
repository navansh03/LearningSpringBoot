package com.navansh.LearningSpringBoot.service.impl;

import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.entity.Student;
import com.navansh.LearningSpringBoot.exception.BadRequestException;
import com.navansh.LearningSpringBoot.exception.ResourceNotFoundException;
import com.navansh.LearningSpringBoot.repository.StudentRepository;
import com.navansh.LearningSpringBoot.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentServiceImplementation implements StudentService {
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    @Override
    public List<StudentDTO> getAllStudents(){
        List<Student>students=studentRepository.findAll();
        return students
                .stream()
                .map(student ->modelMapper.map(student,StudentDTO.class))
                .toList();
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student= studentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Student with this id doesnt exist: "+id));
        return modelMapper.map(student,StudentDTO.class);

    }

    @Override
    public StudentDTO creatNewStudent(AddStudentDTO addStudentDTO) {
        Student newStudent = modelMapper.map(addStudentDTO,Student.class);
        Student student = studentRepository.save(newStudent);
        return modelMapper.map(student,StudentDTO.class);
    }

    @Override
    public void deleteStudentById(Long id) {
        if(!studentRepository.existsById(id)){
            throw new ResourceNotFoundException("Student doesnt exist by id: "+id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    public StudentDTO updateStudentById(Long id, AddStudentDTO addStudentDTO) {
        Student student=studentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Student not found by the id: "+id));
        modelMapper.map(addStudentDTO,student);
        student=studentRepository.save(student);
        return modelMapper.map(student,StudentDTO.class);
    }

    @Override
    public StudentDTO updatePartialStudent(Long id, Map<String, Object> updates) {
        Student student=studentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Student not found by the id: "+id));
        Student finalStudent = student;
        updates.forEach((field, value)-> {
            switch (field){
                case "name":
                    finalStudent.setName((String) value);
                    break;
                case "email" :
                    finalStudent.setEmail((String) value);
                    break;
                default:
                    throw new BadRequestException("Field is not supported");

            }
        });
        student=studentRepository.save(student);
        return modelMapper.map(student,StudentDTO.class);
    }
}
