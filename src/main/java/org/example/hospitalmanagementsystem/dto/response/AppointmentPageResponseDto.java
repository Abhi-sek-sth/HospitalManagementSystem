package org.example.hospitalmanagementsystem.dto.response;

import java.util.List;

public record AppointmentPageResponseDto<T>(

        List<T> content,
        int currentPage,
        long totalElements,
        int totalPages

) {
}
