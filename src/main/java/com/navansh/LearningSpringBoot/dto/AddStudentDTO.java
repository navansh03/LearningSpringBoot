package com.navansh.LearningSpringBoot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AddStudentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    @NotBlank(message = "Name is Required")
    @Size(min=3, max=40,message = "name should be of length 3 to 40 characters")
    private String name;

    @Email
    @NotBlank(message = "Email is Required.")
    private String email;
}
