package org.example.hospitalmanagementsystem.dto.response;

import java.util.List;

public record DoctorPageResponseDto<T>(
        List<T> content,
        int currentPage,
        long totalElements,
        int totalPages

) {
}
