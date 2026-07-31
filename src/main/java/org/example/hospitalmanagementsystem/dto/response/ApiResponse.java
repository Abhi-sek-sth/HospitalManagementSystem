package org.example.hospitalmanagementsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public record ApiResponse<T>(

        @JsonInclude(JsonInclude.Include.NON_NULL)
//         if status is null do not show null in frontend
        int status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data,

        LocalDateTime timestamp) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> successfully(String message) {
        return new ApiResponse<>(200, message, null,LocalDateTime.now());
    }
}
