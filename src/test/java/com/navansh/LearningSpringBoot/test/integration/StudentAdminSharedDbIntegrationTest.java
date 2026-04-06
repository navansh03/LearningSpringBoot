package com.navansh.LearningSpringBoot.test.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navansh.LearningSpringBoot.dto.LoginRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentAdminSharedDbIntegrationTest extends BaseSharedDbIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> usernamesToCleanup = new ArrayList<>();
    private final List<String> studentEmailsToCleanup = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (String email : studentEmailsToCleanup) {
            jdbcTemplate.update("DELETE FROM student WHERE email = ?", email);
        }
        studentEmailsToCleanup.clear();

        for (String username : usernamesToCleanup) {
            jdbcTemplate.update("DELETE FROM users WHERE username = ?", username);
        }
        usernamesToCleanup.clear();
    }

    @Test
    void createStudent_asUser_returnsForbidden() throws Exception {
        String username = uniqueValue("it_user_");
        String password = "Password@123";
        String email = uniqueValue("it_user_mail_") + "@test.com";
        usernamesToCleanup.add(username);

        // Register normal user through API to keep this path realistic.
        LoginRequestDTO registerRequest = new LoginRequestDTO(username, password);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String userToken = loginAndExtractToken(username, password);

        Map<String, Object> createPayload = Map.of(
                "name", "Regular Integration User",
                "email", email
        );

        mockMvc.perform(post("/student")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPatchDeleteStudent_asAdmin_succeeds() throws Exception {
        String adminUsername = uniqueValue("it_admin_");
        String adminPassword = "Admin@123";
        String studentEmail = uniqueValue("it_student_mail_") + "@test.com";
        usernamesToCleanup.add(adminUsername);
        studentEmailsToCleanup.add(studentEmail);

        createAdminUser(adminUsername, adminPassword);
        String adminToken = loginAndExtractToken(adminUsername, adminPassword);

        Map<String, Object> createPayload = Map.of(
                "name", "Admin Created Student",
                "email", studentEmail
        );

        MvcResult createResult = mockMvc.perform(post("/student")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(studentEmail))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long studentId = createdJson.get("id").asLong();

        Map<String, Object> patchPayload = Map.of("name", "Admin Updated Student");

        mockMvc.perform(patch("/student/{id}", studentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Updated Student"));

        mockMvc.perform(delete("/student/{id}", studentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Avoid duplicate cleanup when record was deleted in the flow.
        studentEmailsToCleanup.remove(studentEmail);
    }

    private void createAdminUser(String username, String rawPassword) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                username,
                passwordEncoder.encode(rawPassword),
                "ROLE_ADMIN"
        );
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(username, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return loginJson.get("token").asText();
    }

    private String uniqueValue(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}

