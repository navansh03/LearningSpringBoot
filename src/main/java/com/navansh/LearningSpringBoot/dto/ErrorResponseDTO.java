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
public class ErrorResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private int status;
    private String message;
    private LocalDateTime timestamp;

}
