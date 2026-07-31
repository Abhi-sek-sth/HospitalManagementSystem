package org.example.hospitalmanagementsystem.dto.response;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalElements

) {

}
