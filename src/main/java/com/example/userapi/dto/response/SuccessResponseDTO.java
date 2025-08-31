package com.example.userapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponseDTO {
    private String message;
    private Object data;

    public static SuccessResponseDTO withMessage(String message) {
        return SuccessResponseDTO.builder()
                .message(message)
                .build();
    }

    public static SuccessResponseDTO withData(String message, Object data) {
        return SuccessResponseDTO.builder()
                .message(message)
                .data(data)
                .build();
    }
}
