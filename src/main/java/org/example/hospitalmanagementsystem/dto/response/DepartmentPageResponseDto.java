package org.example.hospitalmanagementsystem.dto.response;

import java.util.List;

public record DepartmentPageResponseDto<T>(
        List<T> content,
        int currentPage,
        long totalElements,
        int totalPages

) {
}
