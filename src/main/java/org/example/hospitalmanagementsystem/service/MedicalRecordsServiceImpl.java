package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.MedicalRecordsRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicalRecordsResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.MedicalRecordsMapper;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.MedicalRecordsRepository;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordsServiceImpl implements MedicalRecordsService{
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordsRepository medicalRecordsRepository;
    private final MedicalRecordsMapper medicalRecordsMapper;
    private final SecurityUtil securityUtil;

    @Transactional
    @Override
    public MedicalRecordsResponseDto createMedicalRecord(MedicalRecordsRequestDto request) {

        Appointment appointment = appointmentRepository.findById(request.appointmentId()).orElseThrow(() -> new ResourceNotFound("Appointment not found with id: " + request.appointmentId()));

        if(appointment.getStatus() != AppointmentStatus.COMPLETED){
            throw new AppointmentNotCompletedException( "Medical record can only be created for a completed appointment");
        }

        if (medicalRecordsRepository.existsByAppointmentId(request.appointmentId())) {
            throw new AlreadyExistException("Medical record already exists for appointment id: " + request.appointmentId());
        }
        if(securityUtil.isAdmin()){

        }
        else if(securityUtil.isDoctor()) {
            Long userId = securityUtil.getCurrentUserId();
            Long doctorUserId = appointment.getDoctor().getUser().getId();
            if (!userId.equals(doctorUserId)) {
                throw new AccessDeniedExceptionHandler("You can only create a medical record for your own appointment");
            }
        }else {
            throw new AccessDeniedExceptionHandler("You do not have permission to create medical records");
        }

        MedicalRecords medicalRecords = medicalRecordsMapper.toEntity(request);

        medicalRecords.setAppointment(appointment);

        MedicalRecords savedRecord = medicalRecordsRepository.save(medicalRecords);

        return medicalRecordsMapper.toResponseDto(savedRecord);

    }

    @Transactional(readOnly = true)
    @Override
    public MedicalRecordsResponseDto getMedicalRecordById(Long id) {
        MedicalRecords medicalRecord = medicalRecordsRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Medical record not found with id: " + id));
        assertCanView(medicalRecord);
        return medicalRecordsMapper.toResponseDto(medicalRecord);
    }
    @Transactional(readOnly = true)
    @Override
    public MedicalRecordsResponseDto getMedicalRecordByAppointmentId(Long appointmentId) {
        MedicalRecords medicalRecord = medicalRecordsRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new ResourceNotFound("Medical record not found for appointment id: " + appointmentId));
        assertCanView(medicalRecord);
        return medicalRecordsMapper.toResponseDto(medicalRecord);
    }
    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<MedicalRecordsResponseDto> getAllMedicalRecords(int page, int size) {
        if (!securityUtil.isAdmin()) {
            throw new AccessDeniedExceptionHandler("You do not have permission to view all medical records");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<MedicalRecords> medicalRecords = medicalRecordsRepository.findAll(pageable);

        return new PageResponseDto<>(
                medicalRecords.getContent()
                        .stream()
                        .map(medicalRecordsMapper::toResponseDto)
                        .toList(),
                medicalRecords.getNumber(),
                medicalRecords.getTotalPages(),
                medicalRecords.getTotalElements()

        );
    }
    @Transactional
    @Override
    public MedicalRecordsResponseDto updateMedicalRecord(Long id, MedicalRecordsRequestDto request) {
        MedicalRecords medicalRecord = medicalRecordsRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Medical record not found with id: " + id));

        Long currentUserId = securityUtil.getCurrentUserId();

        Long appointmentDoctorUserId = medicalRecord.getAppointment().getDoctor().getUser().getId();
        if(securityUtil.isAdmin()){

        }
         else if (securityUtil.isDoctor()) {
            if (!currentUserId.equals(appointmentDoctorUserId)) {
                throw new AccessDeniedExceptionHandler("You can only update medical records for your own appointments");
            }
        } else  {
            throw new AccessDeniedExceptionHandler("You do not have permission to update medical records");
        }

        if (!medicalRecord.getAppointment().getId().equals(request.appointmentId())) {
            throw new InvalidOperationException("Appointment cannot be changed for an existing medical record");
        }

        if (request.diagnosis() != null) {
            medicalRecord.setDiagnosis(request.diagnosis());
        }
        if (request.symptoms() != null) {
            medicalRecord.setSymptoms(request.symptoms());
        }
        if (request.treatment() != null) {
            medicalRecord.setTreatment(request.treatment());
        }
        if (request.notes() != null) {
            medicalRecord.setNotes(request.notes());
        }

        MedicalRecords updatedRecord = medicalRecordsRepository.save(medicalRecord);

        return medicalRecordsMapper.toResponseDto(updatedRecord);
    }
    @Transactional
    @Override
    public void deleteMedicalRecord(Long id) {
        MedicalRecords medicalRecord = medicalRecordsRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Medical record not found with id: " + id));

        if (!securityUtil.isAdmin()) {
            throw new AccessDeniedExceptionHandler("You do not have permission to delete medical records");
        }

        medicalRecordsRepository.delete(medicalRecord);
    }

    private void assertCanView(MedicalRecords medicalRecord) {
        Long currentUserId = securityUtil.getCurrentUserId();

        if (securityUtil.isAdmin()) {
            return;
        }

        if (securityUtil.isPatient()) {
            Long patientUserId = medicalRecord.getAppointment().getPatient().getUser().getId();
            if (!currentUserId.equals(patientUserId)) {
                throw new AccessDeniedExceptionHandler("You can only view your own records");
            }
            return;
        }

        if (securityUtil.isDoctor()) {
            Long doctorUserId = medicalRecord.getAppointment().getDoctor().getUser().getId();
            if (!currentUserId.equals(doctorUserId)) {
                throw new AccessDeniedExceptionHandler(
                        "You can only view a medical record for your own appointment only");
            }
            return;
        }

        throw new AccessDeniedExceptionHandler("You do not have permission to view this medical record");
    }

}
