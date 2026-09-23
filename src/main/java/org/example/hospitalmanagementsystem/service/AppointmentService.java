package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.AppointmentRequestDto;
import org.example.hospitalmanagementsystem.dto.request.AppointmentUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentResponseDto;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponseDto create(AppointmentRequestDto requestDto);

    AppointmentResponseDto getById(Long id);

    AppointmentPageResponseDto<AppointmentResponseDto> getAll(int page , int size);

    AppointmentResponseDto update(Long id, AppointmentUpdateRequestDto requestDto);

    void delete(Long id);

    Page<AppointmentResponseDto> getByPatient(Long patientId, Pageable pageable);

    Page<AppointmentResponseDto> getByDoctor(Long doctorId, Pageable pageable);

    AppointmentResponseDto cancel(Long id);

    AppointmentResponseDto updateAppointmentStatus(Long id, AppointmentStatus newStatus);
}
