package com.navansh.LearningSpringBoot.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navansh.LearningSpringBoot.controller.AuthController;
import com.navansh.LearningSpringBoot.dto.LoginRequestDTO;
import com.navansh.LearningSpringBoot.entity.User;
import com.navansh.LearningSpringBoot.exception.GlobalExceptionHandler;
import com.navansh.LearningSpringBoot.repository.UserRepository;
import com.navansh.LearningSpringBoot.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void login_whenValid_returnsToken() throws Exception {
        LoginRequestDTO req = new LoginRequestDTO("navansh", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("navansh", "password"));
        when(userRepository.findByUsername("navansh"))
                .thenReturn(Optional.of(new User(1L, "navansh", "encoded", "ROLE_USER")));
        when(jwtUtil.generateToken("navansh", "ROLE_USER"))
                .thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_whenBadCredentials_returnsBadRequest() throws Exception {
        LoginRequestDTO req = new LoginRequestDTO("navansh", "wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
