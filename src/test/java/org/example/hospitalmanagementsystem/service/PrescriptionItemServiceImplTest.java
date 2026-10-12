package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PrescriptionItemUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PrescriptionItemResponseDto;
import org.example.hospitalmanagementsystem.entity.MedicalRecords;
import org.example.hospitalmanagementsystem.entity.Medication;
import org.example.hospitalmanagementsystem.entity.Prescription;
import org.example.hospitalmanagementsystem.entity.PrescriptionItem;
import org.example.hospitalmanagementsystem.exception.ActiveItemAlreadyExistsException;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.example.hospitalmanagementsystem.exception.InvalidOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.PrescriptionItemMapper;
import org.example.hospitalmanagementsystem.repository.MedicationRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionItemRepository;
import org.example.hospitalmanagementsystem.repository.PrescriptionRepository;
import org.example.hospitalmanagementsystem.util.PrescriptionAccessPolicy;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionItemServiceImplTest {

    private static final Long PRESCRIPTION_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final Long MEDICATION_ID = 5L;
    private static final Long DOCTOR_USER_ID = 50L;
    private static final String UNIQUE_INDEX = "uq_prescription_items_active_medication";
    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T10:30:00Z");
    private static final LocalDateTime FIXED_LOCAL_DATE_TIME = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

    @Mock private PrescriptionItemRepository itemRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private MedicationRepository medicationRepository;
    @Mock private PrescriptionItemMapper itemMapper;
    @Mock private PrescriptionAccessPolicy accessPolicy;
    @Mock private SecurityUtil securityUtil;
    @Mock private Clock clock;

    @Mock private Prescription prescription;
    @Mock private MedicalRecords medicalRecord;
    @Mock private Medication medication;
    @Mock private PrescriptionItemUpdateRequestDto updateRequest;

    @InjectMocks
    private PrescriptionItemServiceImpl service;

    private PrescriptionItemRequestDto addRequest;
    private PrescriptionItem item;
    private PrescriptionItemResponseDto responseDto;

    @BeforeEach
    void setUp() {
        addRequest = new PrescriptionItemRequestDto(
                MEDICATION_ID, "500mg", "Twice daily", 7, 14, "Take after meals");

        item = PrescriptionItem.builder()
                .id(ITEM_ID)
                .dosage("500mg")
                .frequency("Twice daily")
                .durationDays(7)
                .quantity(14)
                .instructions("Take after meals")
                .build();

        responseDto = new PrescriptionItemResponseDto(
                ITEM_ID, PRESCRIPTION_ID, MEDICATION_ID, "Amoxicillin", "Pfizer",
                "500mg", "Twice daily", 7, 14, "Take after meals",
                FIXED_INSTANT, FIXED_INSTANT);
    }


    private void givenCurrentUser() {
        when(securityUtil.getCurrentUserId()).thenReturn(DOCTOR_USER_ID);
    }

    private void givenPrescriptionHasRecord() {
        when(prescription.getMedicalRecord()).thenReturn(medicalRecord);
    }

    private void givenPrescriptionFound() {
        when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                .thenReturn(Optional.of(prescription));
    }

    private void givenItemWithPrescription() {
        item.setPrescription(prescription);
        when(itemRepository.findByIdWithContext(ITEM_ID)).thenReturn(Optional.of(item));
    }

    private static DataIntegrityViolationException uniqueViolation(String constraintName) {
        SQLException sql = new SQLException("duplicate key value", "23505");
        ConstraintViolationException cve =
                new ConstraintViolationException("duplicate key", sql, constraintName);
        return new DataIntegrityViolationException("could not execute statement", cve);
    }


    @Nested
    @DisplayName("addItem")
    class AddItem {

        private void givenValidPath() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            when(medicationRepository.findById(MEDICATION_ID)).thenReturn(Optional.of(medication));
            when(medication.isActive()).thenReturn(true);
            when(medication.getId()).thenReturn(MEDICATION_ID);
            when(itemRepository.existsByPrescriptionIdAndMedicationId(PRESCRIPTION_ID, MEDICATION_ID))
                    .thenReturn(false);
            when(itemMapper.toEntity(addRequest)).thenReturn(item);
        }

        @Test
        @DisplayName("shouldReturnResponse_whenAllConditionsAreMet")
        void shouldReturnResponse_whenAllConditionsAreMet() {
            givenValidPath();
            when(itemRepository.saveAndFlush(item)).thenReturn(item);
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            PrescriptionItemResponseDto result = service.addItem(PRESCRIPTION_ID, addRequest);

            assertSame(responseDto, result);
            verify(accessPolicy).requireDoctorRole("modify");
            verify(securityUtil).getCurrentUserId();
            verify(prescriptionRepository).findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID);
            verify(itemRepository).saveAndFlush(item);
        }

        @Test
        @DisplayName("shouldCallRequireCanModifyWithRecordUserAndMessage_whenAdding")
        void shouldCallRequireCanModifyWithRecordUserAndMessage_whenAdding() {
            givenValidPath();
            when(prescription.getId()).thenReturn(PRESCRIPTION_ID);
            when(itemRepository.saveAndFlush(item)).thenReturn(item);
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            service.addItem(PRESCRIPTION_ID, addRequest);

            ArgumentCaptor<Supplier<String>> captor = ArgumentCaptor.forClass(Supplier.class);
            verify(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), captor.capture());
            assertEquals("You are not allowed to modify items of prescription with id " + PRESCRIPTION_ID,
                    captor.getValue().get());
        }

        @Test
        @DisplayName("shouldAssignPrescriptionAndMedicationToItem_beforePersisting")
        void shouldAssignPrescriptionAndMedicationToItem_beforePersisting() {
            givenValidPath();
            when(itemRepository.saveAndFlush(any(PrescriptionItem.class))).thenAnswer(inv -> {
                PrescriptionItem arg = inv.getArgument(0);
                assertSame(prescription, arg.getPrescription());
                assertSame(medication, arg.getMedication());
                return arg;
            });
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            service.addItem(PRESCRIPTION_ID, addRequest);

            assertSame(prescription, item.getPrescription());
            assertSame(medication, item.getMedication());
        }

        @Test
        @DisplayName("shouldMapSavedItemToResponse_whenSaveReturnsDifferentInstance")
        void shouldMapSavedItemToResponse_whenSaveReturnsDifferentInstance() {
            givenValidPath();
            PrescriptionItem saved = PrescriptionItem.builder().id(999L).build();
            when(itemRepository.saveAndFlush(item)).thenReturn(saved);
            when(itemMapper.toResponseDto(saved)).thenReturn(responseDto);

            PrescriptionItemResponseDto result = service.addItem(PRESCRIPTION_ID, addRequest);

            assertSame(responseDto, result);
            verify(itemMapper).toResponseDto(saved);
            verify(itemMapper, never()).toResponseDto(item);
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenUserIsNotDoctor")
        void shouldPropagateForbidden_whenUserIsNotDoctor() {
            ForbiddenOperationException ex = new ForbiddenOperationException("Only doctors can modify prescriptions");
            doThrow(ex).when(accessPolicy).requireDoctorRole("modify");

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertSame(ex, thrown);
            verifyNoInteractions(securityUtil, prescriptionRepository, medicationRepository, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenPrescriptionDoesNotExist")
        void shouldThrowResourceNotFound_whenPrescriptionDoesNotExist() {
            givenCurrentUser();
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, ex.getMessage());
            verify(accessPolicy, never()).requireCanModify(any(), any(), any());
            verifyNoInteractions(medicationRepository, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenPrescriptionIsSoftDeleted")
        void shouldThrowResourceNotFound_whenPrescriptionIsSoftDeleted() {
            givenCurrentUser();
            givenPrescriptionFound();
            when(prescription.isDeleted()).thenReturn(true);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, ex.getMessage());
            verify(accessPolicy, never()).requireCanModify(any(), any(), any());
            verifyNoInteractions(medicationRepository, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenDoctorCannotModifyPrescription")
        void shouldPropagateForbidden_whenDoctorCannotModifyPrescription() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            ForbiddenOperationException ex = new ForbiddenOperationException("denied");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertSame(ex, thrown);
            verifyNoInteractions(medicationRepository, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted")
        void shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            AppointmentNotCompletedException ex =
                    new AppointmentNotCompletedException("Your appointment has not been completed yet");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            AppointmentNotCompletedException thrown = assertThrows(AppointmentNotCompletedException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertSame(ex, thrown);
            verifyNoInteractions(medicationRepository, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenMedicationDoesNotExist")
        void shouldThrowResourceNotFound_whenMedicationDoesNotExist() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            when(medicationRepository.findById(MEDICATION_ID)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertEquals("Medication not found with id: " + MEDICATION_ID, ex.getMessage());
            verifyNoInteractions(itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowInvalidOperation_whenMedicationIsInactive")
        void shouldThrowInvalidOperation_whenMedicationIsInactive() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            when(medicationRepository.findById(MEDICATION_ID)).thenReturn(Optional.of(medication));
            when(medication.isActive()).thenReturn(false);
            when(medication.getId()).thenReturn(MEDICATION_ID);

            InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertEquals("Medication with id " + MEDICATION_ID + " is inactive and cannot be prescribed",
                    ex.getMessage());
            verifyNoInteractions(itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowAlreadyExist_whenMedicationAlreadyInPrescription")
        void shouldThrowAlreadyExist_whenMedicationAlreadyInPrescription() {
            givenCurrentUser();
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            when(medicationRepository.findById(MEDICATION_ID)).thenReturn(Optional.of(medication));
            when(medication.isActive()).thenReturn(true);
            when(medication.getId()).thenReturn(MEDICATION_ID);
            when(itemRepository.existsByPrescriptionIdAndMedicationId(PRESCRIPTION_ID, MEDICATION_ID))
                    .thenReturn(true);

            AlreadyExistException ex = assertThrows(AlreadyExistException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertEquals("Medication with id " + MEDICATION_ID + " is already part of prescription "
                    + PRESCRIPTION_ID, ex.getMessage());
            verifyNoInteractions(itemMapper);
            verify(itemRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("shouldThrowActiveItemAlreadyExists_whenUniqueIndexViolated")
        void shouldThrowActiveItemAlreadyExists_whenUniqueIndexViolated() {
            givenValidPath();
            when(itemRepository.saveAndFlush(item)).thenThrow(uniqueViolation(UNIQUE_INDEX));

            assertThrows(ActiveItemAlreadyExistsException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            verify(itemMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("shouldRethrowOriginal_whenViolationIsOnDifferentConstraint")
        void shouldRethrowOriginal_whenViolationIsOnDifferentConstraint() {
            givenValidPath();
            DataIntegrityViolationException other = uniqueViolation("some_other_constraint");
            when(itemRepository.saveAndFlush(item)).thenThrow(other);

            DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertSame(other, thrown);
            verify(itemMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("shouldRethrowOriginal_whenViolationHasNoRecognisableCause")
        void shouldRethrowOriginal_whenViolationHasNoRecognisableCause() {
            givenValidPath();
            DataIntegrityViolationException plain = new DataIntegrityViolationException("boom");
            when(itemRepository.saveAndFlush(item)).thenThrow(plain);

            DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                    () -> service.addItem(PRESCRIPTION_ID, addRequest));

            assertSame(plain, thrown);
        }
    }


    @Nested
    @DisplayName("getItemById")
    class GetItemById {

        @Test
        @DisplayName("shouldReturnMappedResponse_whenItemExistsAndUserCanView")
        void shouldReturnMappedResponse_whenItemExistsAndUserCanView() {
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            givenCurrentUser();
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            PrescriptionItemResponseDto result = service.getItemById(ITEM_ID);

            assertSame(responseDto, result);
            verify(itemRepository).findByIdWithContext(ITEM_ID);
            verify(accessPolicy).requireCanView(medicalRecord, DOCTOR_USER_ID);
            verify(itemMapper).toResponseDto(item);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenItemDoesNotExist")
        void shouldThrowResourceNotFound_whenItemDoesNotExist() {
            when(itemRepository.findByIdWithContext(ITEM_ID)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.getItemById(ITEM_ID));

            assertEquals("Prescription item not found with id: " + ITEM_ID, ex.getMessage());
            verifyNoInteractions(itemMapper, accessPolicy, securityUtil);
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenUserCannotView")
        void shouldPropagateForbidden_whenUserCannotView() {
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            givenCurrentUser();
            ForbiddenOperationException ex =
                    new ForbiddenOperationException("You are not allowed to access this appointment");
            doThrow(ex).when(accessPolicy).requireCanView(medicalRecord, DOCTOR_USER_ID);

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.getItemById(ITEM_ID));

            assertSame(ex, thrown);
            verifyNoInteractions(itemMapper);
        }
    }


    @Nested
    @DisplayName("getItemsByPrescriptionId")
    class GetItemsByPrescriptionId {

        @Test
        @DisplayName("shouldReturnMappedDtosInOrder_whenPrescriptionExistsAndUserCanView")
        void shouldReturnMappedDtosInOrder_whenPrescriptionExistsAndUserCanView() {
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            givenCurrentUser();

            PrescriptionItem second = PrescriptionItem.builder().id(101L).dosage("10mg").build();
            PrescriptionItemResponseDto secondDto = new PrescriptionItemResponseDto(
                    101L, PRESCRIPTION_ID, 6L, "Ibuprofen", "Bayer", "10mg", "Once daily", 3, 3,
                    null, FIXED_INSTANT, FIXED_INSTANT);

            when(itemRepository.findAllByPrescriptionIdWithMedication(PRESCRIPTION_ID))
                    .thenReturn(List.of(item, second));
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);
            when(itemMapper.toResponseDto(second)).thenReturn(secondDto);

            List<PrescriptionItemResponseDto> result = service.getItemsByPrescriptionId(PRESCRIPTION_ID);

            assertEquals(2, result.size());
            assertEquals(List.of(responseDto, secondDto), result);
            verify(accessPolicy).requireCanView(medicalRecord, DOCTOR_USER_ID);
            verify(itemRepository).findAllByPrescriptionIdWithMedication(PRESCRIPTION_ID);
            verify(itemMapper).toResponseDto(item);
            verify(itemMapper).toResponseDto(second);
        }

        @Test
        @DisplayName("shouldReturnEmptyList_whenPrescriptionHasNoItems")
        void shouldReturnEmptyList_whenPrescriptionHasNoItems() {
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            givenCurrentUser();
            when(itemRepository.findAllByPrescriptionIdWithMedication(PRESCRIPTION_ID)).thenReturn(List.of());

            List<PrescriptionItemResponseDto> result = service.getItemsByPrescriptionId(PRESCRIPTION_ID);

            assertTrue(result.isEmpty());
            verifyNoInteractions(itemMapper);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenPrescriptionDoesNotExist")
        void shouldThrowResourceNotFound_whenPrescriptionDoesNotExist() {
            when(prescriptionRepository.findByIdWithRecordAppointmentAndParticipants(PRESCRIPTION_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.getItemsByPrescriptionId(PRESCRIPTION_ID));

            assertEquals("Prescription not found with id: " + PRESCRIPTION_ID, ex.getMessage());
            verifyNoInteractions(itemRepository, itemMapper, accessPolicy, securityUtil);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenPrescriptionIsSoftDeleted")
        void shouldThrowResourceNotFound_whenPrescriptionIsSoftDeleted() {
            givenPrescriptionFound();
            when(prescription.isDeleted()).thenReturn(true);

            assertThrows(ResourceNotFoundException.class,
                    () -> service.getItemsByPrescriptionId(PRESCRIPTION_ID));

            verifyNoInteractions(itemRepository, itemMapper, accessPolicy, securityUtil);
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenUserCannotView")
        void shouldPropagateForbidden_whenUserCannotView() {
            givenPrescriptionFound();
            givenPrescriptionHasRecord();
            givenCurrentUser();
            ForbiddenOperationException ex = new ForbiddenOperationException("denied");
            doThrow(ex).when(accessPolicy).requireCanView(medicalRecord, DOCTOR_USER_ID);

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.getItemsByPrescriptionId(PRESCRIPTION_ID));

            assertSame(ex, thrown);
            verifyNoInteractions(itemRepository, itemMapper);
        }
    }


    @Nested
    @DisplayName("updateItem")
    class UpdateItem {

        @Test
        @DisplayName("shouldUpdateAndReturnResponse_whenRequestIsValidAndDoctorCanModify")
        void shouldUpdateAndReturnResponse_whenRequestIsValidAndDoctorCanModify() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(true);
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            when(itemRepository.saveAndFlush(item)).thenReturn(item);
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            PrescriptionItemResponseDto result = service.updateItem(ITEM_ID, updateRequest);

            assertSame(responseDto, result);
            verify(accessPolicy).requireDoctorRole("modify");
            verify(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());
            verify(itemMapper).updateEntity(updateRequest, item);
            verify(itemRepository).saveAndFlush(item);
            verify(itemRepository, never()).save(any());
            verify(itemMapper).toResponseDto(item);
        }

        @Test
        @DisplayName("shouldApplyUpdateBeforeFlushing")
        void shouldApplyUpdateBeforeFlushing() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(true);
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            when(itemRepository.saveAndFlush(item)).thenReturn(item);
            when(itemMapper.toResponseDto(item)).thenReturn(responseDto);

            service.updateItem(ITEM_ID, updateRequest);

            InOrder inOrder = inOrder(accessPolicy, itemMapper, itemRepository);
            inOrder.verify(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());
            inOrder.verify(itemMapper).updateEntity(updateRequest, item);
            inOrder.verify(itemRepository).saveAndFlush(item);
        }

        @Test
        @DisplayName("shouldThrowInvalidOperation_whenRequestHasNoFields")
        void shouldThrowInvalidOperation_whenRequestHasNoFields() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(false);

            InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                    () -> service.updateItem(ITEM_ID, updateRequest));

            assertEquals("At least one field must be provided for update", ex.getMessage());
            verify(accessPolicy).requireDoctorRole("modify");
            verify(accessPolicy, never()).requireCanModify(any(), any(), any());
            verifyNoInteractions(itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenUserIsNotDoctor")
        void shouldPropagateForbidden_whenUserIsNotDoctor() {
            ForbiddenOperationException ex = new ForbiddenOperationException("Only doctors can modify prescriptions");
            doThrow(ex).when(accessPolicy).requireDoctorRole("modify");

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.updateItem(ITEM_ID, updateRequest));

            assertSame(ex, thrown);
            verifyNoInteractions(securityUtil, itemRepository, itemMapper);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenItemDoesNotExist")
        void shouldThrowResourceNotFound_whenItemDoesNotExist() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(true);
            when(itemRepository.findByIdWithContext(ITEM_ID)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.updateItem(ITEM_ID, updateRequest));

            assertEquals("Prescription item not found with id: " + ITEM_ID, ex.getMessage());
            verify(accessPolicy, never()).requireCanModify(any(), any(), any());
            verifyNoInteractions(itemMapper);
            verify(itemRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenDoctorCannotModifyPrescription")
        void shouldPropagateForbidden_whenDoctorCannotModifyPrescription() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(true);
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            ForbiddenOperationException ex = new ForbiddenOperationException("denied");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            ForbiddenOperationException thrown = assertThrows(ForbiddenOperationException.class,
                    () -> service.updateItem(ITEM_ID, updateRequest));

            assertSame(ex, thrown);
            verifyNoInteractions(itemMapper);
            verify(itemRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted")
        void shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted() {
            givenCurrentUser();
            when(updateRequest.hasAnyField()).thenReturn(true);
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            AppointmentNotCompletedException ex =
                    new AppointmentNotCompletedException("Your appointment has not been completed yet");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            assertSame(ex, assertThrows(AppointmentNotCompletedException.class,
                    () -> service.updateItem(ITEM_ID, updateRequest)));

            verifyNoInteractions(itemMapper);
            verify(itemRepository, never()).saveAndFlush(any());
        }
    }


    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("shouldSoftDeleteItem_whenDoctorCanModify")
        void shouldSoftDeleteItem_whenDoctorCanModify() {
            givenCurrentUser();
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            when(clock.instant()).thenReturn(FIXED_INSTANT);
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);

            service.deleteItem(ITEM_ID);

            assertTrue(item.isDeleted());
            assertEquals(FIXED_LOCAL_DATE_TIME, item.getDeletedAt());
            assertEquals(DOCTOR_USER_ID, item.getDeletedBy());
            verify(accessPolicy).requireDoctorRole("modify");
            verify(securityUtil).getCurrentUserId();
            verify(itemRepository).flush();
        }

        @Test
        @DisplayName("shouldCheckPermissionBeforeDeletingAndFlushAfter")
        void shouldCheckPermissionBeforeDeletingAndFlushAfter() {
            givenCurrentUser();
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            when(clock.instant()).thenReturn(FIXED_INSTANT);
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);

            service.deleteItem(ITEM_ID);

            InOrder inOrder = inOrder(accessPolicy, itemRepository);
            inOrder.verify(accessPolicy).requireDoctorRole("modify");
            inOrder.verify(itemRepository).findByIdWithContext(ITEM_ID);
            inOrder.verify(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());
            inOrder.verify(itemRepository).flush();
        }

        @Test
        @DisplayName("shouldNotHardDelete_whenDeletingItem")
        void shouldNotHardDelete_whenDeletingItem() {
            givenCurrentUser();
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            when(clock.instant()).thenReturn(FIXED_INSTANT);
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);

            service.deleteItem(ITEM_ID);

            verify(itemRepository, never()).delete(any(PrescriptionItem.class));
            verify(itemRepository, never()).deleteById(any());
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenUserIsNotDoctor")
        void shouldPropagateForbidden_whenUserIsNotDoctor() {
            ForbiddenOperationException ex = new ForbiddenOperationException("Only doctors can modify prescriptions");
            doThrow(ex).when(accessPolicy).requireDoctorRole("modify");

            assertSame(ex, assertThrows(ForbiddenOperationException.class,
                    () -> service.deleteItem(ITEM_ID)));

            verifyNoInteractions(securityUtil, itemRepository);
        }

        @Test
        @DisplayName("shouldThrowResourceNotFound_whenItemDoesNotExist")
        void shouldThrowResourceNotFound_whenItemDoesNotExist() {
            givenCurrentUser();
            when(itemRepository.findByIdWithContext(ITEM_ID)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.deleteItem(ITEM_ID));

            assertEquals("Prescription item not found with id: " + ITEM_ID, ex.getMessage());
            verify(accessPolicy, never()).requireCanModify(any(), any(), any());
            verify(itemRepository, never()).flush();
        }

        @Test
        @DisplayName("shouldPropagateForbidden_whenDoctorCannotModifyPrescription")
        void shouldPropagateForbidden_whenDoctorCannotModifyPrescription() {
            givenCurrentUser();
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            ForbiddenOperationException ex = new ForbiddenOperationException("denied");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            assertSame(ex, assertThrows(ForbiddenOperationException.class,
                    () -> service.deleteItem(ITEM_ID)));

            assertFalse(item.isDeleted());
            assertNull(item.getDeletedAt());
            assertNull(item.getDeletedBy());
            verify(itemRepository, never()).flush();
        }

        @Test
        @DisplayName("shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted")
        void shouldPropagateAppointmentNotCompleted_whenAppointmentIsNotCompleted() {
            givenCurrentUser();
            givenItemWithPrescription();
            givenPrescriptionHasRecord();
            AppointmentNotCompletedException ex =
                    new AppointmentNotCompletedException("Your appointment has not been completed yet");
            doThrow(ex).when(accessPolicy).requireCanModify(eq(medicalRecord), eq(DOCTOR_USER_ID), any());

            assertSame(ex, assertThrows(AppointmentNotCompletedException.class,
                    () -> service.deleteItem(ITEM_ID)));

            assertFalse(item.isDeleted());
            verify(itemRepository, never()).flush();
        }
    }
}