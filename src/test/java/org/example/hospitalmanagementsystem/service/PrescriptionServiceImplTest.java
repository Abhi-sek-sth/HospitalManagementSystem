package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.example.hospitalmanagementsystem.entity.Prescription;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.PrescriptionMapper;
import org.example.hospitalmanagementsystem.repository.MedicalRecordsRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.PrescriptionAccessPolicy;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    private static final Long USER_ID = 7L;
    private static final Long MEDICAL_RECORD_ID = 10L;
    private static final Long PRESCRIPTION_ID = 500L;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-06T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 10, 6, 10, 0);

    private static final String NOT_COMPLETED_MESSAGE = "Your appointment has not been completed yet";

    @Mock
    private MedicalRecordsRepository medicalRecordsRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionAccessPolicy accessPolicy;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private PrescriptionRequestDto request;

    @Mock
    private PrescriptionUpdateRequestDto updateRequest;

    @Mock
    private PrescriptionResponseDto response;

    @Captor
    private ArgumentCaptor<Prescription> prescriptionCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private PrescriptionServiceImpl prescriptionService;

    private Doctor doctor;
    private Appointment appointment;
    private MedicalRecords medicalRecord;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionServiceImpl(
                medicalRecordsRepository,
                prescriptionRepository,
                prescriptionMapper,
                accessPolicy,
                securityUtil,
                FIXED_CLOCK);

        // Prescription -> MedicalRecord -> Appointment -> Doctor
        doctor = new Doctor();
        appointment = new Appointment();
        appointment.setDoctor(doctor);
        medicalRecord = new MedicalRecords();
        medicalRecord.setId(MEDICAL_RECORD_ID);
        medicalRecord.setAppointment(appointment);
        prescription = new Prescription();
        prescription.setMedicalRecord(medicalRecord);
    }


    private void stubMedicalRecordLookup() {
        when(request.medicalRecordId()).thenReturn(MEDICAL_RECORD_ID);
        when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
        when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID))
                .thenReturn(Optional.of(medicalRecord));
    }

    private void stubPrescriptionLookupForUpdateOrDelete() {
        when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
        when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                .thenReturn(Optional.of(prescription));
    }

    private void stubEmptyPageForMedicalRecord() {
        when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID))
                .thenReturn(Optional.of(medicalRecord));
        when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
        when(prescriptionRepository.findAllByMedicalRecordId(eq(MEDICAL_RECORD_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
    }

    private void stubOwnershipCheckToFail() {
        doAnswer(invocation -> {
            Supplier<String> message = invocation.getArgument(2);
            throw new ForbiddenOperationException(message.get());
        }).when(accessPolicy).requireOwningDoctor(eq(appointment), eq(USER_ID), any());
    }

    @Nested
    @DisplayName("createPrescription")
    class CreatePrescription {

        @Test
        @DisplayName("should create prescription when doctor is active and owns completed medical record")
        void shouldCreatePrescriptionSuccessfully() {
            Prescription mapped = new Prescription();
            Prescription saved = new Prescription();
            saved.setId(PRESCRIPTION_ID);

            stubMedicalRecordLookup();
            when(prescriptionMapper.toEntity(request)).thenReturn(mapped);
            when(prescriptionRepository.save(mapped)).thenReturn(saved);
            when(prescriptionMapper.toResponseDto(saved)).thenReturn(response);

            PrescriptionResponseDto result = prescriptionService.createPrescription(request);

            assertSame(response, result);

            verify(prescriptionRepository).save(prescriptionCaptor.capture());
            Prescription persisted = prescriptionCaptor.getValue();
            assertSame(mapped, persisted);
            assertSame(medicalRecord, persisted.getMedicalRecord());

            verify(accessPolicy).requireDoctorRole("create");
            verify(accessPolicy).requireOwningDoctor(eq(appointment), eq(USER_ID), any());
            verify(accessPolicy).requireCompleted(appointment);
            verify(accessPolicy).requireActiveDoctor(doctor, "Doctor is inactive and cannot create prescriptions");
            verify(prescriptionMapper).toResponseDto(saved);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not doctor")
        void shouldThrowForbiddenWhenNotDoctor() {
            doThrow(new ForbiddenOperationException("Only doctors can create prescriptions"))
                    .when(accessPolicy).requireDoctorRole("create");

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals("Only doctors can create prescriptions", exception.getMessage());

            verifyNoInteractions(medicalRecordsRepository, prescriptionRepository, prescriptionMapper, securityUtil);
            verifyNoMoreInteractions(accessPolicy);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medical record does not exist")
        void shouldThrowResourceNotFoundWhenMedicalRecordDoesNotExist() {
            when(request.medicalRecordId()).thenReturn(MEDICAL_RECORD_ID);
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals("Medical record not found with id: " + MEDICAL_RECORD_ID, exception.getMessage());

            verify(accessPolicy).requireDoctorRole("create");
            verifyNoMoreInteractions(accessPolicy);
            verifyNoInteractions(prescriptionMapper, prescriptionRepository);
        }

        @Test
        @DisplayName("should throw AppointmentNotCompletedException when medical record has no appointment")
        void shouldThrowNotCompletedWhenMedicalRecordHasNoAppointment() {
            medicalRecord.setAppointment(null);
            stubMedicalRecordLookup();

            AppointmentNotCompletedException exception = assertThrows(
                    AppointmentNotCompletedException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals(NOT_COMPLETED_MESSAGE, exception.getMessage());

            verify(accessPolicy).requireDoctorRole("create");
            verifyNoMoreInteractions(accessPolicy);
            verifyNoInteractions(prescriptionMapper, prescriptionRepository);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when doctor does not own medical record")
        void shouldThrowForbiddenWhenDoctorDoesNotOwnMedicalRecord() {
            stubMedicalRecordLookup();
            stubOwnershipCheckToFail();

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals("You are not allowed to prescribe medication for this medical record", exception.getMessage());

            verify(accessPolicy, never()).requireCompleted(any());
            verify(accessPolicy, never()).requireActiveDoctor(any(), any());
            verifyNoInteractions(prescriptionMapper, prescriptionRepository);
        }

        @Test
        @DisplayName("should throw AppointmentNotCompletedException when appointment is not completed")
        void shouldThrowNotCompletedWhenAppointmentIsNotCompleted() {
            stubMedicalRecordLookup();
            doThrow(new AppointmentNotCompletedException(NOT_COMPLETED_MESSAGE))
                    .when(accessPolicy).requireCompleted(appointment);

            AppointmentNotCompletedException exception = assertThrows(
                    AppointmentNotCompletedException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals(NOT_COMPLETED_MESSAGE, exception.getMessage());

            verify(accessPolicy).requireOwningDoctor(eq(appointment), eq(USER_ID), any());
            verify(accessPolicy, never()).requireActiveDoctor(any(), any());
            verifyNoInteractions(prescriptionMapper, prescriptionRepository);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when doctor is inactive")
        void shouldThrowForbiddenWhenDoctorIsInactive() {
            String message = "Doctor is inactive and cannot create prescriptions";
            stubMedicalRecordLookup();
            doThrow(new ForbiddenOperationException(message))
                    .when(accessPolicy).requireActiveDoctor(doctor, message);

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> prescriptionService.createPrescription(request)
            );

            assertEquals(message, exception.getMessage());

            verify(accessPolicy).requireCompleted(appointment);
            verifyNoInteractions(prescriptionMapper, prescriptionRepository);
        }
    }

    @Nested
    @DisplayName("getPrescriptionById")
    class GetPrescriptionById {

        @Test
        @DisplayName("should return prescription when current user is participant")
        void shouldReturnPrescriptionWhenParticipant() {
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.of(prescription));
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(response);

            PrescriptionResponseDto result = prescriptionService.getPrescriptionById(PRESCRIPTION_ID);

            assertSame(response, result);

            verify(accessPolicy).requireParticipant(appointment, USER_ID);
            verify(prescriptionMapper).toResponseDto(prescription);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when prescription does not exist")
        void shouldThrowResourceNotFoundWhenPrescriptionDoesNotExist() {
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class, () -> prescriptionService.getPrescriptionById(PRESCRIPTION_ID)
            );

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, exception.getMessage());

            verifyNoInteractions(accessPolicy, prescriptionMapper, securityUtil);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not participant")
        void shouldThrowForbiddenWhenNotParticipant() {
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.of(prescription));
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            doThrow(new ForbiddenOperationException("You are not allowed to access this appointment"))
                    .when(accessPolicy).requireParticipant(appointment, USER_ID);

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> prescriptionService.getPrescriptionById(PRESCRIPTION_ID)
            );

            assertEquals("You are not allowed to access this appointment", exception.getMessage());

            verifyNoInteractions(prescriptionMapper);
        }
    }

    @Nested
    @DisplayName("getPrescriptionsByMedicalRecordId")
    class GetPrescriptionsByMedicalRecordId {

        @Test
        @DisplayName("should return mapped page when current user is participant")
        void shouldReturnMappedPageWhenParticipant() {
            Prescription second = new Prescription();
            PrescriptionResponseDto firstDto = mock(PrescriptionResponseDto.class);
            PrescriptionResponseDto secondDto = mock(PrescriptionResponseDto.class);

            when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID)).thenReturn(Optional.of(medicalRecord));
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            when(prescriptionRepository.findAllByMedicalRecordId(eq(MEDICAL_RECORD_ID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(prescription, second), PageRequest.of(0, 20), 2));
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(firstDto);
            when(prescriptionMapper.toResponseDto(second)).thenReturn(secondDto);

            PageResponseDto<PrescriptionResponseDto> result = prescriptionService.getPrescriptionsByMedicalRecordId(MEDICAL_RECORD_ID, 0, 20);

            assertEquals(2, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals(0, result.currentPage());
            assertEquals(List.of(firstDto, secondDto), result.content());

            verify(accessPolicy).requireParticipant(appointment, USER_ID);
            verify(prescriptionMapper).toResponseDto(prescription);
            verify(prescriptionMapper).toResponseDto(second);
        }

        @Test
        @DisplayName("should sort by prescribedAt descending when querying repository")
        void shouldSortByPrescribedAtDescending() {
            stubEmptyPageForMedicalRecord();

            prescriptionService.getPrescriptionsByMedicalRecordId(MEDICAL_RECORD_ID, 0, 20);

            verify(prescriptionRepository).findAllByMedicalRecordId(eq(MEDICAL_RECORD_ID), pageableCaptor.capture());
            assertEquals(Sort.by("prescribedAt").descending(), pageableCaptor.getValue().getSort());
        }

        @ParameterizedTest(name = "page={0}, size={1} -> page={2}, size={3}")
        @DisplayName("should normalize page and size when values are out of range")
        @CsvSource({
                "-5,   0,    0, 1",
                "0,  -10,    0, 1",
                "3, 1000,    3, 100",
                "0,  101,    0, 100",
                "0,  100,    0, 100",
                "2,   50,    2, 50"
        })
        void shouldNormalizePageAndSize(int page, int size, int expectedPage, int expectedSize) {
            stubEmptyPageForMedicalRecord();

            prescriptionService.getPrescriptionsByMedicalRecordId(MEDICAL_RECORD_ID, page, size);

            verify(prescriptionRepository).findAllByMedicalRecordId(eq(MEDICAL_RECORD_ID), pageableCaptor.capture());
            assertEquals(expectedPage, pageableCaptor.getValue().getPageNumber());
            assertEquals(expectedSize, pageableCaptor.getValue().getPageSize());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medical record does not exist")
        void shouldThrowResourceNotFoundWhenMedicalRecordDoesNotExist() {
            when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> prescriptionService.getPrescriptionsByMedicalRecordId(MEDICAL_RECORD_ID, 0, 20)
            );

            assertEquals("Medical record not found with id: " + MEDICAL_RECORD_ID, exception.getMessage());

            verifyNoInteractions(accessPolicy, prescriptionRepository, prescriptionMapper, securityUtil);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not participant")
        void shouldThrowForbiddenWhenNotParticipant() {
            when(medicalRecordsRepository.findByIdWithAppointmentAndDoctor(MEDICAL_RECORD_ID))
                    .thenReturn(Optional.of(medicalRecord));
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            doThrow(new ForbiddenOperationException("You are not allowed to access this appointment"))
                    .when(accessPolicy).requireParticipant(appointment, USER_ID);

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class,
                    () -> prescriptionService.getPrescriptionsByMedicalRecordId(MEDICAL_RECORD_ID, 0, 20)
            );

            assertEquals("You are not allowed to access this appointment", exception.getMessage());

            verifyNoInteractions(prescriptionRepository, prescriptionMapper);
        }
    }

    @Nested
    @DisplayName("updatePrescription")
    class UpdatePrescription {

        @Test
        @DisplayName("should update prescription when doctor is active and owns completed appointment")
        void shouldUpdatePrescriptionSuccessfully() {
            stubPrescriptionLookupForUpdateOrDelete();
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(response);

            PrescriptionResponseDto result = prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest);

            assertSame(response, result);

            verify(accessPolicy).requireDoctorRole("modify");
            verify(accessPolicy).requireOwningDoctor(eq(appointment), eq(USER_ID), any());
            verify(accessPolicy).requireCompleted(appointment);
            verify(accessPolicy).requireActiveDoctor(doctor, "Doctor is inactive and cannot modify prescriptions");
            verify(prescriptionMapper).updateEntity(updateRequest, prescription);
            verify(prescriptionMapper).toResponseDto(prescription);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not doctor")
        void shouldThrowForbiddenWhenNotDoctor() {
            doThrow(new ForbiddenOperationException("Only doctors can modify prescriptions"))
                    .when(accessPolicy).requireDoctorRole("modify");

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals("Only doctors can modify prescriptions", exception.getMessage());

            verifyNoInteractions(prescriptionRepository, prescriptionMapper, securityUtil);
            verifyNoMoreInteractions(accessPolicy);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when prescription does not exist")
        void shouldThrowResourceNotFoundWhenPrescriptionDoesNotExist() {
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, exception.getMessage());

            verify(accessPolicy).requireDoctorRole("modify");
            verifyNoMoreInteractions(accessPolicy);
            verifyNoInteractions(prescriptionMapper);
        }

        @Test
        @DisplayName("should throw AppointmentNotCompletedException when medical record has no appointment")
        void shouldThrowNotCompletedWhenMedicalRecordHasNoAppointment() {
            medicalRecord.setAppointment(null);
            stubPrescriptionLookupForUpdateOrDelete();

            AppointmentNotCompletedException exception = assertThrows(
                    AppointmentNotCompletedException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals(NOT_COMPLETED_MESSAGE, exception.getMessage());

            verify(accessPolicy).requireDoctorRole("modify");
            verifyNoMoreInteractions(accessPolicy);
            verifyNoInteractions(prescriptionMapper);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when doctor does not own prescription")
        void shouldThrowForbiddenWhenDoctorDoesNotOwnPrescription() {
            stubPrescriptionLookupForUpdateOrDelete();
            stubOwnershipCheckToFail();

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals("You are not allowed to update this prescription with id " + PRESCRIPTION_ID,
                    exception.getMessage());

            verify(accessPolicy, never()).requireCompleted(any());
            verify(accessPolicy, never()).requireActiveDoctor(any(), any());
            verifyNoInteractions(prescriptionMapper);
        }

        @Test
        @DisplayName("should throw AppointmentNotCompletedException when appointment is not completed")
        void shouldThrowNotCompletedWhenAppointmentIsNotCompleted() {
            stubPrescriptionLookupForUpdateOrDelete();
            doThrow(new AppointmentNotCompletedException(NOT_COMPLETED_MESSAGE))
                    .when(accessPolicy).requireCompleted(appointment);

            AppointmentNotCompletedException exception = assertThrows(
                    AppointmentNotCompletedException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals(NOT_COMPLETED_MESSAGE, exception.getMessage());

            verify(accessPolicy, never()).requireActiveDoctor(any(), any());
            verifyNoInteractions(prescriptionMapper);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when doctor is inactive")
        void shouldThrowForbiddenWhenDoctorIsInactive() {
            String message = "Doctor is inactive and cannot modify prescriptions";
            stubPrescriptionLookupForUpdateOrDelete();
            doThrow(new ForbiddenOperationException(message))
                    .when(accessPolicy).requireActiveDoctor(doctor, message);

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class,
                    () -> prescriptionService.updatePrescription(PRESCRIPTION_ID, updateRequest)
            );

            assertEquals(message, exception.getMessage());

            verifyNoInteractions(prescriptionMapper);
        }
    }

    @Nested
    @DisplayName("deletePrescription")
    class DeletePrescription {

        @Test
        @DisplayName("should soft delete prescription when doctor owns prescription")
        void shouldSoftDeletePrescriptionSuccessfully() {
            stubPrescriptionLookupForUpdateOrDelete();

            prescriptionService.deletePrescription(PRESCRIPTION_ID);

            assertEquals(true, ReflectionTestUtils.getField(prescription, "deleted"));
            assertEquals(FIXED_NOW, ReflectionTestUtils.getField(prescription, "deletedAt"));
            assertEquals(USER_ID, ReflectionTestUtils.getField(prescription, "deletedBy"));

            verify(accessPolicy).requireDoctorRole("delete");
            verify(accessPolicy).requireOwningDoctor(eq(appointment), eq(USER_ID), any());
            verify(prescriptionRepository, never()).save(any());
            verifyNoInteractions(prescriptionMapper);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not doctor")
        void shouldThrowForbiddenWhenNotDoctor() {
            doThrow(new ForbiddenOperationException("Only doctors can delete prescriptions"))
                    .when(accessPolicy).requireDoctorRole("delete");

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> prescriptionService.deletePrescription(PRESCRIPTION_ID)
            );

            assertEquals("Only doctors can delete prescriptions", exception.getMessage());

            verifyNoInteractions(prescriptionRepository, securityUtil);
            verifyNoMoreInteractions(accessPolicy);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when prescription does not exist")
        void shouldThrowResourceNotFoundWhenPrescriptionDoesNotExist() {
            when(securityUtil.getCurrentUserId()).thenReturn(USER_ID);
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class, () -> prescriptionService.deletePrescription(PRESCRIPTION_ID)
            );

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, exception.getMessage());

            verify(accessPolicy).requireDoctorRole("delete");
            verifyNoMoreInteractions(accessPolicy);
        }

        @Test
        @DisplayName("should not mark prescription as deleted when doctor does not own prescription")
        void shouldNotMarkAsDeletedWhenDoctorDoesNotOwnPrescription() {
            stubPrescriptionLookupForUpdateOrDelete();
            stubOwnershipCheckToFail();

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> prescriptionService.deletePrescription(PRESCRIPTION_ID)
            );

            assertEquals("You are not allowed to delete this prescription with id " + PRESCRIPTION_ID,
                    exception.getMessage());

            assertNotEquals(true, ReflectionTestUtils.getField(prescription, "deleted"));
            assertNull(ReflectionTestUtils.getField(prescription, "deletedAt"));
            assertNull(ReflectionTestUtils.getField(prescription, "deletedBy"));
        }
    }
}