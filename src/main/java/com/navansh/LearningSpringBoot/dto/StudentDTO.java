package com.navansh.LearningSpringBoot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private String name;
    private Long id;
    private String email;
    private LocalDateTime createdAt=LocalDateTime.now();

//    public StudentDTO(String name, Long id, String email) {
//        this.name = name;
//        this.id = id;
//        this.email = email;
//    }
//
//    public StudentDTO() {
//    }
}
