package com.navansh.LearningSpringBoot.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navansh.LearningSpringBoot.controller.StudentController;
import com.navansh.LearningSpringBoot.dto.AddStudentDTO;
import com.navansh.LearningSpringBoot.dto.StudentDTO;
import com.navansh.LearningSpringBoot.exception.GlobalExceptionHandler;
import com.navansh.LearningSpringBoot.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
@Import({GlobalExceptionHandler.class, StudentControllerTest.MethodSecurityTestConfig.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @MockitoBean private StudentService studentService;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "USER")
    void getStudents_asUser_returnsOk() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(
                new StudentDTO("A", 1L, "a@test.com", LocalDateTime.now())
        ));

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createStudent_asUser_returnsForbidden() throws Exception {
        AddStudentDTO req = new AddStudentDTO();
        req.setName("User One");
        req.setEmail("user1@test.com");
        mockMvc.perform(post("/student")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createStudent_asAdmin_returnsCreated() throws Exception {
        AddStudentDTO req = new AddStudentDTO();
        req.setName("Admin User");
        req.setEmail("admin@test.com");

        when(studentService.creatNewStudent(any(AddStudentDTO.class)))
                .thenReturn(new StudentDTO("Admin User", 11L, "admin@test.com", LocalDateTime.now()));

        mockMvc.perform(post("/student")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));
    }
}
