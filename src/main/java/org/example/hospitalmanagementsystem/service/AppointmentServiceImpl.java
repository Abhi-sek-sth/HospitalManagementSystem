package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.AppointmentRequestDto;
import org.example.hospitalmanagementsystem.dto.request.AppointmentUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.Patient;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.AppointmentMapper;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.example.hospitalmanagementsystem.repository.PatientRepository;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService{
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;
    private final CurrentUserService currentUserService;
    private final SecurityUtil securityUtil;
    private static final int MAX_PAGE_SIZE = 100;
    @Transactional
    @Override
    public AppointmentResponseDto create(AppointmentRequestDto requestDto) {

        LocalDateTime appointmentDateTime = LocalDateTime.of(requestDto.appointmentDate(), requestDto.appointmentTime());

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new TimeAlreadyFinishException(
                    "Appointment date and time cannot be in the past"
            );
        }

        Patient patient = patientRepository.findById(requestDto.patientId()).orElseThrow(() -> new ResourceNotFound("Patient Not Found"));

        Doctor doctor = doctorRepository.findById(requestDto.doctorId()).orElseThrow(() -> new ResourceNotFound("Doctor Not Found"));

        if (!doctor.getActive()) {
            throw new DoctorInactiveException("Doctor is currently inactive");
        }

        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                                requestDto.doctorId(),
                                requestDto.appointmentDate(),
                                requestDto.appointmentTime()
                        );

        if (alreadyBooked) {
            throw new AlreadyExistException("Doctor already has an appointment at this time");
        }

        Appointment appointment = appointmentMapper.toEntity(requestDto);

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.PENDING);
try {
    Appointment savedAppointment = appointmentRepository.save(appointment);

    return appointmentMapper.toResponseDto(savedAppointment);
}catch (DataIntegrityViolationException e){
    throw new AlreadyExistException("Doctor already has an appointment at this time");

}
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Appointment Not Found"));
        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
        if (!isStaff) {
            Long userId = currentUserService.getCurrentUserId();
            boolean isOwner = (currentUserService.isDoctor() && userId.equals(appointment.getDoctor().getUser().getId()))
                    || (currentUserService.isPatient() && userId.equals(appointment.getPatient().getUser().getId()));
            if (!isOwner) {
                throw new AccessDeniedExceptionHandler("You are not allowed to access this appointment");
            }
        }
        return appointmentMapper.toResponseDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentPageResponseDto<AppointmentResponseDto> getAll(int page , int size) {

        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
        if (!isStaff) {
            throw new AccessDeniedExceptionHandler("You are not allowed to access all appointments");
        }

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());

        Page<Appointment> appointments = appointmentRepository.findAll(pageable);

        return new AppointmentPageResponseDto<>(
                appointments.getContent()
                        .stream()
                        .map(appointmentMapper::toResponseDto)
                        .toList(),
                appointments.getNumber(),
                appointments.getTotalElements(),
                appointments.getTotalPages()

        );
    }
@Transactional
    @Override
    public AppointmentResponseDto update(Long id, AppointmentUpdateRequestDto requestDto) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Appointment not found"));

    boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
    if (!isStaff) {
        Long userId = currentUserService.getCurrentUserId();
        boolean isOwner = (currentUserService.isPatient() && userId.equals(appointment.getPatient().getUser().getId()))
                || (currentUserService.isDoctor() && userId.equals(appointment.getDoctor().getUser().getId()));

        if (!isOwner) {
            throw new AccessDeniedExceptionHandler("You are not allowed to update this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new InvalidOperationException("You can only update an appointment while it is still pending");
        }
    }

    boolean timeChanged = !appointment.getAppointmentDate().equals(requestDto.appointmentDate()) || !appointment.getAppointmentTime().equals(requestDto.appointmentTime());

        if (timeChanged) {
            if (!appointment.getDoctor().getActive()) {
                throw new DoctorInactiveException("Doctor is currently inactive");
            }
            LocalDateTime newDateTime = LocalDateTime.of(requestDto.appointmentDate(), requestDto.appointmentTime());
            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new TimeAlreadyFinishException("Appointment date and time cannot be in the past");
            }

            boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                                    appointment.getDoctor().getId(),
                                    requestDto.appointmentDate(),
                                    requestDto.appointmentTime()
                            );

            if (alreadyBooked) {
                throw new AlreadyExistException(
                        "Doctor already has an appointment at this time"
                );
            }
        }
        appointment.setAppointmentDate(requestDto.appointmentDate());

        appointment.setAppointmentTime(requestDto.appointmentTime());

        appointment.setReason(requestDto.reason());

        appointment.setNotes(requestDto.notes());

    Appointment saveAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toResponseDto(saveAppointment);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Appointment not found"));

        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
        if (!isStaff) {
            throw new AccessDeniedExceptionHandler("You are not allowed to delete this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot delete a completed appointment");
        }
        appointmentRepository.delete(appointment);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AppointmentResponseDto> getByPatient(Long patientId, Pageable pageable) {

        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFound("Patient not found"));
        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
        if(!isStaff){
            if(!currentUserService.isPatient()) {
                throw new AccessDeniedExceptionHandler("You are not allowed to access patient appointments");
            }
        Long userId =  currentUserService.getCurrentUserId();
                if(!userId.equals(patient.getUser().getId())){
                    throw new AccessDeniedExceptionHandler("You are not allowed to access another patient's appointments");
}}
        return appointmentRepository.findByPatientId(patientId, pageable)
                .map(appointmentMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AppointmentResponseDto> getByDoctor(Long doctorId, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new ResourceNotFound("Doctor not found"));

        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();
        if(!isStaff){
            if(!currentUserService.isDoctor()) {
                throw new AccessDeniedExceptionHandler("You are not allowed to access doctor appointments");
            }
                Long userId =  currentUserService.getCurrentUserId();
                if(!userId.equals(doctor.getUser().getId())){
                    throw new AccessDeniedExceptionHandler("You are not allowed to access another doctor appointments");
         }
        }
        return appointmentRepository.findByDoctorId(doctorId, pageable)
                .map(appointmentMapper::toResponseDto);
    }

    @Transactional
    @Override
    public AppointmentResponseDto cancel(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Appointment not found"));

        boolean isStaff = currentUserService.isAdmin() || currentUserService.isReceptionist();

        if (!isStaff) {
            if (!currentUserService.isPatient()) {
                throw new AccessDeniedExceptionHandler("You are not allowed to cancel this appointment");
            }

            Long userId = currentUserService.getCurrentUserId();
            boolean isOwner = userId.equals(appointment.getPatient().getUser().getId());

            if (!isOwner) {
                throw new AccessDeniedExceptionHandler("You are not allowed to cancel another patient's appointment");
            }

            if (appointment.getStatus() != AppointmentStatus.PENDING) {
                throw new InvalidOperationException("You can only cancel an appointment while it is still pending");
            }
        } else {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                throw new InvalidOperationException("Cannot cancel a completed appointment");
            }
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        return appointmentMapper.toResponseDto(saved);
    }
    @Override
    @Transactional
    public AppointmentResponseDto updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Appointment not found with id: " + id));
        if(securityUtil.isAdmin()){

        }
        else if(securityUtil.isDoctor()) {
            Long userId = securityUtil.getCurrentUserId();
            Long doctorUserId = appointment.getDoctor().getUser().getId();
                if (!userId.equals(doctorUserId)) {
                 throw new AccessDeniedExceptionHandler("You can only update a status for your own appointment");
            }
        }

        AppointmentStatus currentStatus = appointment.getStatus();

        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == AppointmentStatus.CONFIRMED;

            case CONFIRMED -> newStatus == AppointmentStatus.COMPLETED;

            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if (!validTransition) {
            throw new InvalidOperationException("Invalid appointment status transition from " + currentStatus + " to " + newStatus);
        }

        appointment.setStatus(newStatus);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment)
        );
    }
}
