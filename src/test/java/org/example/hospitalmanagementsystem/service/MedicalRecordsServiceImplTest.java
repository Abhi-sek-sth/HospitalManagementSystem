package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.MedicalRecordUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicalRecordsRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicalRecordsResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.*;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.MedicalRecordsMapper;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.MedicalRecordsRepository;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordsService Unit Test")
public class MedicalRecordsServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordsRepository medicalRecordsRepository;

    @Mock
    private MedicalRecordsMapper medicalRecordsMapper;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MedicalRecordsServiceImpl medicalRecordsService;

    private static final Long DOCTOR_USER_ID = 10L;
    private static final Long PATIENT_USER_ID = 20L;
    private static final Long OTHER_USER_ID = 99L;

    private Register doctorUser;
    private Register patientUser;
    private Department department;
    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;
    private MedicalRecords medicalRecords;
    private MedicalRecordsRequestDto medicalRecordsRequestDto;
    private MedicalRecordsResponseDto medicalRecordsResponseDto;

    @BeforeEach
    void setUp() {
        doctorUser = Register.builder()
                .id(DOCTOR_USER_ID)
                .build();

        patientUser = Register.builder()
                .id(PATIENT_USER_ID)
                .build();

        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .location("4th Floor")
                .description("Heart Department")
                .createdAt(LocalDateTime.now())
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .phone("9812345678")
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .gender(Gender.Male)
                .address("Banepa, Kavre")
                .active(true)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .licenseNumber("12345")
                .phone("1234567890")
                .user(doctorUser)
                .department(department)
                .specialization("Cardiology")
                .active(true)
                .createdAt(Instant.now())
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(LocalDate.now().minusDays(1))
                .appointmentTime(LocalTime.of(10, 30))
                .reason("Fever and headache")
                .notes("Patient has had symptoms for two days")
                .status(AppointmentStatus.COMPLETED)
                .build();

        medicalRecords = new MedicalRecords();
        medicalRecords.setAppointment(appointment);
        medicalRecords.setDiagnosis("Viral fever");
        medicalRecords.setSymptoms("Fever and headache");
        medicalRecords.setTreatment("Paracetamol");
        medicalRecords.setNotes("Drink plenty of fluids");

        // Mocked (not constructed) because the exact constructor of these records isn't needed by the tests.
        medicalRecordsRequestDto = mock(MedicalRecordsRequestDto.class);
        lenient().when(medicalRecordsRequestDto.appointmentId()).thenReturn(1L);

        medicalRecordsResponseDto = mock(MedicalRecordsResponseDto.class);
    }

    @Nested
    @DisplayName("Create Medical Record")
    class CreateMedicalRecord {

        @Test
        @DisplayName("Should create medical record by admin")
        void shouldCreateMedicalRecordByAdmin() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(medicalRecordsRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicalRecordsMapper.toEntity(medicalRecordsRequestDto)).thenReturn(medicalRecords);
            when(medicalRecordsRepository.save(medicalRecords)).thenReturn(medicalRecords);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto);

            assertEquals(medicalRecordsResponseDto, result);
            assertSame(appointment, medicalRecords.getAppointment());

            verify(appointmentRepository).findById(1L);
            verify(medicalRecordsRepository).existsByAppointmentId(1L);
            verify(medicalRecordsMapper).toEntity(medicalRecordsRequestDto);
            verify(medicalRecordsRepository).save(medicalRecords);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should create medical record by doctor for own appointment")
        void shouldCreateMedicalRecordByDoctorForOwnAppointment() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(medicalRecordsRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);
            when(securityUtil.getCurrentUserId()).thenReturn(DOCTOR_USER_ID);
            when(medicalRecordsMapper.toEntity(medicalRecordsRequestDto)).thenReturn(medicalRecords);
            when(medicalRecordsRepository.save(medicalRecords)).thenReturn(medicalRecords);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto);

            assertEquals(medicalRecordsResponseDto, result);
            assertSame(appointment, medicalRecords.getAppointment());

            verify(medicalRecordsRepository).save(medicalRecords);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should throw ResourceNotFound when appointment does not exist")
        void shouldThrowResourceNotFoundWhenAppointmentDoesNotExist() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto));

            assertEquals("Appointment not found with id: 1", exception.getMessage());

            verify(appointmentRepository).findById(1L);
            verifyNoInteractions(medicalRecordsRepository, medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AppointmentNotCompletedException when appointment is not completed")
        void shouldThrowAppointmentNotCompletedException() {
            appointment.setStatus(AppointmentStatus.PENDING);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            AppointmentNotCompletedException exception = assertThrows(AppointmentNotCompletedException.class,
                    () -> medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto));

            assertEquals("Medical record can only be created for a completed appointment", exception.getMessage());

            verify(appointmentRepository).findById(1L);
            verifyNoInteractions(medicalRecordsRepository, medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AlreadyExistException when medical record already exists")
        void shouldThrowAlreadyExistExceptionWhenRecordAlreadyExists() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(medicalRecordsRepository.existsByAppointmentId(1L)).thenReturn(true);

            AlreadyExistException exception = assertThrows(AlreadyExistException.class,
                    () -> medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto));

            assertEquals("Medical record already exists for appointment id: 1", exception.getMessage());

            verify(medicalRecordsRepository).existsByAppointmentId(1L);
            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when doctor is not owner of the appointment")
        void shouldThrowAccessDeniedWhenDoctorIsNotOwner() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(medicalRecordsRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto));

            assertEquals("You can only create a medical record for your own appointment", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is neither admin nor doctor")
        void shouldThrowAccessDeniedWhenUserHasNoValidRole() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(medicalRecordsRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.createMedicalRecord(medicalRecordsRequestDto));

            assertEquals("You do not have permission to create medical records", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper);
        }
    }

    @Nested
    @DisplayName("Get Medical Record By Id")
    class GetMedicalRecordById {

        @Test
        @DisplayName("Should get medical record by admin")
        void shouldGetMedicalRecordByIdByAdmin() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.getMedicalRecordById(1L);

            assertEquals(medicalRecordsResponseDto, result);

            verify(medicalRecordsRepository).findById(1L);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should get own medical record by patient")
        void shouldGetMedicalRecordByIdWhenPatientIsOwner() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(PATIENT_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(true);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.getMedicalRecordById(1L);

            assertEquals(medicalRecordsResponseDto, result);

            verify(medicalRecordsRepository).findById(1L);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should get medical record of own appointment by doctor")
        void shouldGetMedicalRecordByIdWhenDoctorIsOwner() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(DOCTOR_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.getMedicalRecordById(1L);

            assertEquals(medicalRecordsResponseDto, result);

            verify(medicalRecordsRepository).findById(1L);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should throw ResourceNotFound when medical record does not exist")
        void shouldThrowResourceNotFoundWhenRecordDoesNotExist() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> medicalRecordsService.getMedicalRecordById(1L));

            assertEquals("Medical record not found with id: 1", exception.getMessage());

            verify(medicalRecordsRepository).findById(1L);
            verifyNoInteractions(medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when patient is not owner")
        void shouldThrowAccessDeniedWhenPatientIsNotOwner() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(true);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.getMedicalRecordById(1L));

            assertEquals("You can only view your own records", exception.getMessage());

            verify(medicalRecordsRepository).findById(1L);
            verifyNoInteractions(medicalRecordsMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when doctor is not owner")
        void shouldThrowAccessDeniedWhenDoctorIsNotOwner() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.getMedicalRecordById(1L));

            assertEquals("You can only view a medical record for your own appointment only", exception.getMessage());

            verify(medicalRecordsRepository).findById(1L);
            verifyNoInteractions(medicalRecordsMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user has no valid role")
        void shouldThrowAccessDeniedWhenUserHasNoValidRole() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.getMedicalRecordById(1L));

            assertEquals("You do not have permission to view this medical record", exception.getMessage());

            verify(medicalRecordsMapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("Get Medical Record By Appointment Id")
    class GetMedicalRecordByAppointmentId {

        @Test
        @DisplayName("Should get medical record by appointment id")
        void shouldGetMedicalRecordByAppointmentId() {
            when(medicalRecordsRepository.findByAppointmentId(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.getMedicalRecordByAppointmentId(1L);

            assertEquals(medicalRecordsResponseDto, result);

            verify(medicalRecordsRepository).findByAppointmentId(1L);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should throw ResourceNotFound when no medical record exists for appointment")
        void shouldThrowResourceNotFoundWhenRecordDoesNotExist() {
            when(medicalRecordsRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> medicalRecordsService.getMedicalRecordByAppointmentId(1L));

            assertEquals("Medical record not found for appointment id: 1", exception.getMessage());

            verify(medicalRecordsRepository).findByAppointmentId(1L);
            verifyNoInteractions(medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when patient is not owner")
        void shouldThrowAccessDeniedWhenPatientIsNotOwner() {
            when(medicalRecordsRepository.findByAppointmentId(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isPatient()).thenReturn(true);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.getMedicalRecordByAppointmentId(1L));

            assertEquals("You can only view your own records", exception.getMessage());

            verifyNoInteractions(medicalRecordsMapper);
        }
    }

    @Nested
    @DisplayName("Get All Medical Records")
    class GetAllMedicalRecords {

        @Test
        @DisplayName("Should retrieve all medical records when user is admin")
        void shouldRetrieveAllMedicalRecordsWhenUserIsAdmin() {
            Page<MedicalRecords> recordsPage = new PageImpl<>(List.of(medicalRecords), PageRequest.of(0, 5), 1);

            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicalRecordsRepository.findAll(any(Pageable.class))).thenReturn(recordsPage);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            PageResponseDto<MedicalRecordsResponseDto> result = medicalRecordsService.getAllMedicalRecords(0, 5);

            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals(1, result.content().size());
            assertEquals(medicalRecordsResponseDto, result.content().get(0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(medicalRecordsRepository).findAll(pageableCaptor.capture());

            Pageable usedPageable = pageableCaptor.getValue();
            assertEquals(0, usedPageable.getPageNumber());
            assertEquals(5, usedPageable.getPageSize());
            assertEquals(Sort.Direction.DESC, usedPageable.getSort().getOrderFor("createdAt").getDirection());

            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should not retrieve all medical records when user is not admin")
        void shouldNotRetrieveAllMedicalRecordsWhenUserIsNotAdmin() {
            when(securityUtil.isAdmin()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.getAllMedicalRecords(0, 5));

            assertEquals("You do not have permission to view all medical records", exception.getMessage());

            verifyNoInteractions(medicalRecordsRepository, medicalRecordsMapper);
        }
    }

    @Nested
    @DisplayName("Update Medical Record")
    class UpdateMedicalRecord {

        @Test
        @DisplayName("Should update all fields when doctor updates own appointment's record")
        void shouldUpdateAllFieldsByDoctorForOwnAppointment() {
            MedicalRecordUpdateRequestDto updateRequest = new MedicalRecordUpdateRequestDto(
                    "Flu",
                    "Fever, cough",
                    "Rest and fluids",
                    "Follow up in a week"
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(DOCTOR_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);
            when(medicalRecordsRepository.save(medicalRecords)).thenReturn(medicalRecords);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.updateMedicalRecord(1L, updateRequest);

            assertEquals(medicalRecordsResponseDto, result);
            assertEquals("Flu", medicalRecords.getDiagnosis());
            assertEquals("Fever, cough", medicalRecords.getTreatment());
            assertEquals("Rest and fluids", medicalRecords.getSymptoms());
            assertEquals("Follow up in a week", medicalRecords.getNotes());

            verify(medicalRecordsRepository).findById(1L);
            verify(medicalRecordsRepository).save(medicalRecords);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should update only provided field when admin updates")
        void shouldUpdateOnlyProvidedFieldByAdmin() {
            MedicalRecordUpdateRequestDto updateRequest = new MedicalRecordUpdateRequestDto(
                    null,
                    null,
                    null,
                    "Updated notes"
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicalRecordsRepository.save(medicalRecords)).thenReturn(medicalRecords);
            when(medicalRecordsMapper.toResponseDto(medicalRecords)).thenReturn(medicalRecordsResponseDto);

            MedicalRecordsResponseDto result = medicalRecordsService.updateMedicalRecord(1L, updateRequest);

            assertEquals(medicalRecordsResponseDto, result);
            assertEquals("Updated notes", medicalRecords.getNotes());
            assertEquals("Viral fever", medicalRecords.getDiagnosis());
            assertEquals("Fever and headache", medicalRecords.getSymptoms());
            assertEquals("Paracetamol", medicalRecords.getTreatment());

            verify(medicalRecordsRepository).save(medicalRecords);
            verify(medicalRecordsMapper).toResponseDto(medicalRecords);
        }

        @Test
        @DisplayName("Should throw ResourceNotFound when medical record does not exist")
        void shouldThrowResourceNotFoundWhenRecordDoesNotExist() {
            MedicalRecordUpdateRequestDto updateRequest = new MedicalRecordUpdateRequestDto(
                    "Flu", null, null, null
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> medicalRecordsService.updateMedicalRecord(1L, updateRequest));

            assertEquals("Medical record not found", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper, securityUtil);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is neither admin nor doctor")
        void shouldThrowAccessDeniedWhenUserHasNoValidRole() {
            MedicalRecordUpdateRequestDto updateRequest = new MedicalRecordUpdateRequestDto(
                    "Flu", null, null, null
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(PATIENT_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.updateMedicalRecord(1L, updateRequest));

            assertEquals("You do not have permission to update medical records", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when doctor is not owner of the appointment")
        void shouldThrowAccessDeniedWhenDoctorIsNotOwner() {
            MedicalRecordUpdateRequestDto updateRequest = new MedicalRecordUpdateRequestDto(
                    "Flu", null, null, null
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(false);
            when(securityUtil.isDoctor()).thenReturn(true);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.updateMedicalRecord(1L, updateRequest));

            assertEquals("You can only update medical records for your own appointments", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper);
        }

        @Test
        @DisplayName("Should throw InvalidOperationException when no field is provided")
        void shouldThrowInvalidOperationExceptionWhenNoFieldProvided() {
            MedicalRecordUpdateRequestDto emptyRequest = new MedicalRecordUpdateRequestDto(
                    null, null, null, null
            );

            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.getCurrentUserId()).thenReturn(OTHER_USER_ID);
            when(securityUtil.isAdmin()).thenReturn(true);

            InvalidOperationException exception = assertThrows(InvalidOperationException.class,
                    () -> medicalRecordsService.updateMedicalRecord(1L, emptyRequest));

            assertEquals("At least one field must be provided for update", exception.getMessage());

            verify(medicalRecordsRepository, never()).save(any());
            verifyNoInteractions(medicalRecordsMapper);
        }
    }

    @Nested
    @DisplayName("Delete Medical Record")
    class DeleteMedicalRecord {

        @Test
        @DisplayName("Should delete medical record when user is admin")
        void shouldDeleteMedicalRecordWhenUserIsAdmin() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.isAdmin()).thenReturn(true);

            medicalRecordsService.deleteMedicalRecord(1L);

            verify(medicalRecordsRepository).findById(1L);
            verify(medicalRecordsRepository, times(1)).delete(medicalRecords);
        }

        @Test
        @DisplayName("Should throw ResourceNotFound when medical record does not exist")
        void shouldThrowResourceNotFoundWhenRecordDoesNotExist() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> medicalRecordsService.deleteMedicalRecord(1L));

            assertEquals("Medical record not found with id: 1", exception.getMessage());

            verify(medicalRecordsRepository, never()).delete(any());
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not admin")
        void shouldThrowAccessDeniedWhenUserIsNotAdmin() {
            when(medicalRecordsRepository.findById(1L)).thenReturn(Optional.of(medicalRecords));
            when(securityUtil.isAdmin()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class,
                    () -> medicalRecordsService.deleteMedicalRecord(1L));

            assertEquals("You do not have permission to delete medical records", exception.getMessage());

            verify(medicalRecordsRepository, never()).delete(any());
        }
    }
}