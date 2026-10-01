package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.MedicationRequestDto;
import org.example.hospitalmanagementsystem.dto.request.MedicationUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.MedicationResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.Medication;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.example.hospitalmanagementsystem.exception.InvalidOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.MedicationMapper;
import org.example.hospitalmanagementsystem.repository.MedicationRepository;
import org.example.hospitalmanagementsystem.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicationMapper medicationMapper;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    private MedicationRequestDto medicationRequestDto;
    private MedicationUpdateRequestDto medicationUpdateRequestDto;
    private MedicationResponseDto medicationResponseDto;
    private Medication medication;

    @BeforeEach
    void setUp() {
        medicationRequestDto = MedicationRequestDto.builder()
                .name("Paracetamol")
                .manufacturer("Cipla")
                .unitPrice(BigDecimal.valueOf(25.50))
                .build();

        medicationUpdateRequestDto = MedicationUpdateRequestDto.builder()
                .name("Paracetamol 650")
                .manufacturer("Cipla")
                .unitPrice(BigDecimal.valueOf(3.00))
                .build();

        medication = Medication.builder()
                .id(1L)
                .name("Paracetamol")
                .manufacturer("Cipla")
                .unitPrice(BigDecimal.valueOf(2.50))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        medicationResponseDto = MedicationResponseDto.builder()
                .id(1L)
                .name("Paracetamol")
                .manufacturer("Cipla")
                .unitPrice(BigDecimal.valueOf(2.50))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createMedication")
    class CreateMedication {

        @Test
        @DisplayName("should create medication successfully when admin and no duplicate exists")
        void shouldCreateMedicationSuccessfully() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationRequestDto.name(), medicationRequestDto.manufacturer())).thenReturn(false);
            when(medicationMapper.toEntity(medicationRequestDto)).thenReturn(medication);
            when(medicationRepository.save(medication)).thenReturn(medication);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.createMedication(medicationRequestDto);

            assertEquals(medicationResponseDto, result);
            assertTrue(medication.isActive());

            verify(securityUtil).isAdmin();
            verify(medicationRepository).existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationRequestDto.name(), medicationRequestDto.manufacturer());
            verify(medicationMapper).toEntity(medicationRequestDto);
            verify(medicationRepository).save(medication);
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not admin")
        void shouldThrowForbiddenWhenNotAdmin() {
            when(securityUtil.isAdmin()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(
                    ForbiddenOperationException.class, () -> medicationService.createMedication(medicationRequestDto)
            );

            assertEquals("You are not allowed to create or save medicine", exception.getMessage());

            verify(securityUtil).isAdmin();
            verifyNoInteractions(medicationRepository, medicationMapper);
        }

        @Test
        @DisplayName("should throw AlreadyExistException when medication already exists")
        void shouldThrowAlreadyExistWhenDuplicateExists() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationRequestDto.name(), medicationRequestDto.manufacturer())).thenReturn(true);

            AlreadyExistException exception = assertThrows(AlreadyExistException.class, () -> medicationService.createMedication(medicationRequestDto));

            assertTrue(exception.getMessage().contains(medicationRequestDto.name()));

            verify(medicationRepository).existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationRequestDto.name(), medicationRequestDto.manufacturer());
            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }
    }

    @Nested
    @DisplayName("getMedicationById")
    class GetMedicationById {

        @Test
        @DisplayName("should return medication when it is active")
        void shouldReturnMedicationWhenActive() {
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.getMedicationById(1L);

            assertEquals(medicationResponseDto, result);

            verify(medicationRepository).findById(1L);
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medication does not exist")
        void shouldThrowResourceNotFoundWhenMedicationDoesNotExist() {
            when(medicationRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> medicationService.getMedicationById(1L));

            assertEquals("Medication not found with id: 1", exception.getMessage());

            verify(medicationRepository).findById(1L);
            verifyNoInteractions(medicationMapper);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when medication is inactive and user is not admin")
        void shouldThrowForbiddenWhenInactiveAndNotAdmin() {
            medication.setActive(false);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(securityUtil.isAdmin()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> medicationService.getMedicationById(1L));

            assertEquals("Currently we don't have this medicine in our store", exception.getMessage());

            verify(medicationRepository).findById(1L);
            verify(securityUtil).isAdmin();
            verifyNoInteractions(medicationMapper);
        }

        @Test
        @DisplayName("should return inactive medication when user is admin")
        void shouldReturnInactiveMedicationWhenAdmin() {
            medication.setActive(false);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.getMedicationById(1L);

            assertEquals(medicationResponseDto, result);

            verify(medicationRepository).findById(1L);
            verify(securityUtil).isAdmin();
            verify(medicationMapper).toResponse(medication);
        }
    }

    @Nested
    @DisplayName("getAllActiveMedications")
    class GetAllActiveMedications {

        @Test
        @DisplayName("should return a page of active medications")
        void shouldReturnPagedActiveMedications() {
            Page<Medication> medicationPage = new PageImpl<>(List.of(medication), PageRequest.of(0, 10), 1);

            when(medicationRepository.findByActiveTrue(any(Pageable.class))).thenReturn(medicationPage);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            PageResponseDto<MedicationResponseDto> result = medicationService.getAllActiveMedications(0, 10);

            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals(0, result.currentPage());
            assertEquals(1, result.content().size());
            assertEquals(medicationResponseDto, result.content().get(0));

            verify(medicationRepository).findByActiveTrue(any(Pageable.class));
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should clamp negative page to 0 and oversized page size to MAX_PAGE_SIZE")
        void shouldClampInvalidPagingInputs() {
            Page<Medication> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);

            when(medicationRepository.findByActiveTrue(any(Pageable.class))).thenReturn(emptyPage);

            medicationService.getAllActiveMedications(-5, 500);

            verify(medicationRepository).findByActiveTrue(argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 100));
        }
    }

    @Nested
    @DisplayName("updateMedication")
    class UpdateMedication {

        @Test
        @DisplayName("should update medication successfully when admin and fields provided")
        void shouldUpdateMedicationSuccessfully() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationRepository.existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationUpdateRequestDto.name(), medicationUpdateRequestDto.manufacturer())).thenReturn(false);
            when(medicationRepository.save(medication)).thenReturn(medication);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.updateMedication(1L, medicationUpdateRequestDto);

            assertEquals(medicationResponseDto, result);

            verify(securityUtil).isAdmin();
            verify(medicationRepository).findById(1L);
            verify(medicationRepository).existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationUpdateRequestDto.name(), medicationUpdateRequestDto.manufacturer());
            verify(medicationMapper).updateEntity(medicationUpdateRequestDto, medication);
            verify(medicationRepository).save(medication);
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not admin")
        void shouldThrowForbiddenWhenNotAdmin() {
            when(securityUtil.isAdmin()).thenReturn(false);

            ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () -> medicationService.updateMedication(1L, medicationUpdateRequestDto)
            );

            assertEquals("You are not allowed to update medication", exception.getMessage());

            verify(securityUtil).isAdmin();
            verifyNoInteractions(medicationRepository, medicationMapper);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medication does not exist")
        void shouldThrowResourceNotFoundWhenMedicationDoesNotExist() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class, () -> medicationService.updateMedication(1L, medicationUpdateRequestDto)
            );

            assertEquals("Medication not found with id: 1", exception.getMessage());

            verify(medicationRepository).findById(1L);
            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when no fields are provided")
        void shouldThrowInvalidOperationWhenNoFieldsProvided() {
            MedicationUpdateRequestDto emptyRequest = MedicationUpdateRequestDto.builder().build();

            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));

            InvalidOperationException exception = assertThrows(
                    InvalidOperationException.class, () -> medicationService.updateMedication(1L, emptyRequest)
            );

            assertEquals("At least one field must be provided for update", exception.getMessage());

            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }

        @Test
        @DisplayName("should throw AlreadyExistException when new name/manufacturer combination already exists")
        void shouldThrowAlreadyExistWhenDuplicateAfterChange() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationRepository.existsByNameIgnoreCaseAndManufacturerIgnoreCase(
                    medicationUpdateRequestDto.name(), medicationUpdateRequestDto.manufacturer())).thenReturn(true);

            assertThrows(
                    AlreadyExistException.class, () -> medicationService.updateMedication(1L, medicationUpdateRequestDto)
            );

            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }

        @Test
        @DisplayName("should not check for duplicates when name and manufacturer are unchanged")
        void shouldNotCheckDuplicateWhenNameAndManufacturerUnchanged() {
            MedicationUpdateRequestDto priceOnlyUpdate = MedicationUpdateRequestDto.builder()
                    .unitPrice(BigDecimal.valueOf(9.99))
                    .build();

            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationRepository.save(medication)).thenReturn(medication);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            medicationService.updateMedication(1L, priceOnlyUpdate);

            verify(medicationRepository, never()).existsByNameIgnoreCaseAndManufacturerIgnoreCase(any(), any());
            verify(medicationMapper).updateEntity(priceOnlyUpdate, medication);
            verify(medicationRepository).save(medication);
        }
    }

    @Nested
    @DisplayName("deactivateMedication")
    class DeactivateMedication {

        @Test
        @DisplayName("should deactivate medication successfully when admin")
        void shouldDeactivateMedicationSuccessfully() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationRepository.save(medication)).thenReturn(medication);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.deactivateMedication(1L);

            assertEquals(medicationResponseDto, result);
            assertFalse(medication.isActive());

            verify(securityUtil).isAdmin();
            verify(medicationRepository).findById(1L);
            verify(medicationRepository).save(medication);
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not admin")
        void shouldThrowForbiddenWhenNotAdmin() {
            when(securityUtil.isAdmin()).thenReturn(false);

            assertThrows(ForbiddenOperationException.class, () -> medicationService.deactivateMedication(1L));

            verifyNoInteractions(medicationRepository, medicationMapper);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medication does not exist")
        void shouldThrowResourceNotFoundWhenMedicationDoesNotExist() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> medicationService.deactivateMedication(1L));

            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }
    }

    @Nested
    @DisplayName("activateMedication")
    class ActivateMedication {

        @Test
        @DisplayName("should activate medication successfully when admin")
        void shouldActivateMedicationSuccessfully() {
            medication.setActive(false);

            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));
            when(medicationRepository.save(medication)).thenReturn(medication);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            MedicationResponseDto result = medicationService.activateMedication(1L);

            assertEquals(medicationResponseDto, result);
            assertTrue(medication.isActive());

            verify(securityUtil).isAdmin();
            verify(medicationRepository).findById(1L);
            verify(medicationRepository).save(medication);
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw ForbiddenOperationException when user is not admin")
        void shouldThrowForbiddenWhenNotAdmin() {
            when(securityUtil.isAdmin()).thenReturn(false);

            assertThrows(ForbiddenOperationException.class, () -> medicationService.activateMedication(1L));

            verifyNoInteractions(medicationRepository, medicationMapper);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when medication does not exist")
        void shouldThrowResourceNotFoundWhenMedicationDoesNotExist() {
            when(securityUtil.isAdmin()).thenReturn(true);
            when(medicationRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> medicationService.activateMedication(1L));

            verify(medicationRepository, never()).save(any());
            verifyNoInteractions(medicationMapper);
        }
    }

    @Nested
    @DisplayName("searchActiveMedications")
    class SearchActiveMedications {

        @Test
        @DisplayName("should return matching active medications")
        void shouldReturnMatchingMedications() {
            Page<Medication> medicationPage = new PageImpl<>(List.of(medication), PageRequest.of(0, 10), 1);

            when(medicationRepository.findByNameContainingIgnoreCaseAndActiveTrue(eq("Para"), any(Pageable.class)))
                    .thenReturn(medicationPage);
            when(medicationMapper.toResponse(medication)).thenReturn(medicationResponseDto);

            PageResponseDto<MedicationResponseDto> result = medicationService.searchActiveMedications("Para", 0, 10);

            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals(medicationResponseDto, result.content().get(0));

            verify(medicationRepository).findByNameContainingIgnoreCaseAndActiveTrue(eq("Para"), any(Pageable.class));
            verify(medicationMapper).toResponse(medication);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when name is blank")
        void shouldThrowInvalidOperationWhenNameIsBlank() {
            InvalidOperationException exception = assertThrows(
                    InvalidOperationException.class, () -> medicationService.searchActiveMedications("   ", 0, 10)
            );

            assertEquals("Medication name must not be empty", exception.getMessage());

            verifyNoInteractions(medicationRepository, medicationMapper);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when name is null")
        void shouldThrowInvalidOperationWhenNameIsNull() {
            assertThrows(
                    InvalidOperationException.class, () -> medicationService.searchActiveMedications(null, 0, 10)
            );

            verifyNoInteractions(medicationRepository, medicationMapper);
        }
    }
}