package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.PatientRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PatientUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PatientPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PatientResponseDto;
import org.example.hospitalmanagementsystem.entity.Gender;
import org.example.hospitalmanagementsystem.entity.Patient;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.mapper.PatientMapper;
import org.example.hospitalmanagementsystem.repository.PatientRepository;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private RegisterRepository registerRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRequestDto patientRequestDto;
    private PatientUpdateRequestDto patientUpdateRequestDto;
    private Patient patient;
    private PatientResponseDto patientResponseDto;
    private Register register;

    @BeforeEach
    void setUp() {

        register = Register.builder()
                .id(1L)
                .build();

        patient = Patient.builder()
                .id(1L)
                .user(register)
                .phone("9812345678")
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .gender(Gender.Male)
                .address("Banepa, Kavre")
                .active(true)
                .build();

        patientRequestDto = PatientRequestDto.builder()
                .userId(1L)
                .phone("9812345678")
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .gender(Gender.Male)
                .address("Banepa, Kavre")
                .active(true)
                .build();

        patientUpdateRequestDto = PatientUpdateRequestDto.builder()
                .phone("9822222222")
                .dateOfBirth(LocalDate.of(2000, 5, 15))
                .gender(Gender.Male)
                .address("Kathmandu, Nepal")
                .active(true)
                .build();

        patientResponseDto = new PatientResponseDto(
                1L,
                1L,
                "9812345678",
                LocalDate.of(2000, 5, 15),
                Gender.Male,
                "Banepa, Kavre",
                true,
                null,
                null
        );
    }

    @Nested
    class CreatePatient {

        @Test
        void shouldCreatePatientSuccessfully() {

            when(patientRepository.existsByPhone(patientRequestDto.phone()))
                    .thenReturn(false);

            when(registerRepository.findById(patientRequestDto.userId()))
                    .thenReturn(Optional.of(register));

            when(patientMapper.toEntity(patientRequestDto))
                    .thenReturn(patient);

            when(patientRepository.save(patient))
                    .thenReturn(patient);

            when(patientMapper.toResponseDto(patient))
                    .thenReturn(patientResponseDto);

            PatientResponseDto result =
                    patientService.create(patientRequestDto);

            assertNotNull(result);
            assertEquals(patientResponseDto, result);

            verify(patientRepository)
                    .existsByPhone(patientRequestDto.phone());

            verify(registerRepository)
                    .findById(patientRequestDto.userId());

            verify(patientMapper)
                    .toEntity(patientRequestDto);

            verify(patientRepository)
                    .save(patient);

            verify(patientMapper)
                    .toResponseDto(patient);

            assertEquals(register, patient.getUser());

        }


        @Test
        void shouldThrowExceptionWhenPhoneAlreadyExists() {

            when(patientRepository.existsByPhone(patientRequestDto.phone()))
                    .thenReturn(true);

            AlreadyExistException exception =
                    assertThrows(
                            AlreadyExistException.class,
                            () -> patientService.create(patientRequestDto)
                    );

            assertEquals(
                    "Patient with this phone number already exists",
                    exception.getMessage()
            );

            verify(patientRepository)
                    .existsByPhone(patientRequestDto.phone());

            verifyNoInteractions(
                    registerRepository,
                    patientMapper
            );

            verify(patientRepository, never())
                    .save(any(Patient.class));
        }


        @Test
        void shouldThrowResourceNotFoundWhenUserDoesNotExist() {

            when(patientRepository.existsByPhone(patientRequestDto.phone()))
                    .thenReturn(false);

            when(registerRepository.findById(patientRequestDto.userId()))
                    .thenReturn(Optional.empty());

            ResourceNotFound exception =
                    assertThrows(
                            ResourceNotFound.class,
                            () -> patientService.create(patientRequestDto)
                    );

            assertEquals("User Not Found", exception.getMessage());

            verify(patientRepository)
                    .existsByPhone(patientRequestDto.phone());

            verify(registerRepository)
                    .findById(patientRequestDto.userId());

            verifyNoInteractions(patientMapper);

            verify(patientRepository, never())
                    .save(any(Patient.class));
        }
    }

    @Nested
    class GetPatientById {

        @Test
        void shouldReturnPatientWhenPatientExists() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(patientMapper.toResponseDto(patient))
                    .thenReturn(patientResponseDto);

            PatientResponseDto result =
                    patientService.getById(1L);

            assertNotNull(result);
            assertEquals(patientResponseDto, result);

            verify(patientRepository)
                    .findById(1L);

            verify(patientMapper)
                    .toResponseDto(patient);
        }


        @Test
        void shouldThrowResourceNotFoundWhenPatientDoesNotExist() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFound exception =
                    assertThrows(
                            ResourceNotFound.class,
                            () -> patientService.getById(1L)
                    );

            assertEquals(
                    "Patient Not Found",
                    exception.getMessage()
            );

            verify(patientRepository)
                    .findById(1L);

            verifyNoInteractions(patientMapper);
        }
    }

    @Nested
    class GetAllPatients {

        @Test
        void shouldReturnAllPatientsSuccessfully() {

            Patient secondPatient = Patient.builder()
                    .id(2L)
                    .user(register)
                    .phone("9822222222")
                    .dateOfBirth(LocalDate.of(1998, 10, 10))
                    .gender(Gender.Female)
                    .address("Kathmandu")
                    .active(true)
                    .build();

            PatientResponseDto secondResponse =
                    new PatientResponseDto(
                            2L,
                            1L,
                            "9822222222",
                            LocalDate.of(1998, 10, 10),
                            Gender.Female,
                            "Kathmandu",
                            true,
                            null,
                            null
                    );

            List<Patient> patients =
                    List.of(patient, secondPatient);

            PageRequest pageable =
                    PageRequest.of(
                            0,
                            10,
                            Sort.by("createdAt").descending()
                    );

            Page<Patient> patientPage =
                    new PageImpl<>(
                            patients,
                            pageable,
                            2
                    );

            when(patientRepository.findAll(any(PageRequest.class)))
                    .thenReturn(patientPage);

            when(patientMapper.toResponseDto(patient))
                    .thenReturn(patientResponseDto);

            when(patientMapper.toResponseDto(secondPatient))
                    .thenReturn(secondResponse);

            PatientPageResponseDto<PatientResponseDto> result =
                    patientService.getAll(0, 10);

            assertNotNull(result);

            assertEquals(2, result.content().size());
            assertEquals(0, result.currentPage());
            assertEquals(2, result.totalElements());
            assertEquals(1, result.totalPages());

            assertEquals(
                    patientResponseDto,
                    result.content().get(0)
            );

            assertEquals(
                    secondResponse,
                    result.content().get(1)
            );

            verify(patientRepository)
                    .findAll(any(PageRequest.class));

            verify(patientMapper)
                    .toResponseDto(patient);

            verify(patientMapper)
                    .toResponseDto(secondPatient);
        }


        @Test
        void shouldReturnEmptyPageWhenNoPatientsExist() {

            PageRequest pageable =
                    PageRequest.of(
                            0,
                            10,
                            Sort.by("createdAt").descending()
                    );

            Page<Patient> emptyPage =
                    new PageImpl<>(
                            List.of(),
                            pageable,
                            0
                    );

            when(patientRepository.findAll(any(PageRequest.class)))
                    .thenReturn(emptyPage);

            PatientPageResponseDto<PatientResponseDto> result =
                    patientService.getAll(0, 10);

            assertNotNull(result);

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.totalElements());
            assertEquals(0, result.totalPages());

            verify(patientRepository)
                    .findAll(any(PageRequest.class));

            verifyNoInteractions(patientMapper);
        }
    }

    @Nested
    class UpdatePatient {

        @Test
        void shouldUpdatePatientSuccessfully() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(patientRepository.existsByPhoneAndIdNot(
                    patientUpdateRequestDto.phone(),
                    1L
            )).thenReturn(false);

            when(patientRepository.save(patient))
                    .thenReturn(patient);

            when(patientMapper.toResponseDto(patient))
                    .thenReturn(patientResponseDto);

            PatientResponseDto result =
                    patientService.update(
                            1L,
                            patientUpdateRequestDto
                    );

            assertNotNull(result);
            assertEquals(patientResponseDto, result);

            verify(patientRepository)
                    .findById(1L);

            verify(patientRepository)
                    .existsByPhoneAndIdNot(
                            patientUpdateRequestDto.phone(),
                            1L
                    );

            verify(patientMapper)
                    .updateEntity(
                            patientUpdateRequestDto,
                            patient
                    );

            verify(patientRepository)
                    .save(patient);

            verify(patientMapper)
                    .toResponseDto(patient);
        }


        @Test
        void shouldThrowResourceNotFoundWhenPatientDoesNotExist() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFound exception =
                    assertThrows(
                            ResourceNotFound.class,
                            () -> patientService.update(
                                    1L,
                                    patientUpdateRequestDto
                            )
                    );

            assertEquals(
                    "Patient Not Found",
                    exception.getMessage()
            );

            verify(patientRepository)
                    .findById(1L);

            verify(patientRepository, never())
                    .existsByPhoneAndIdNot(anyString(), anyLong());

            verifyNoInteractions(patientMapper);

            verify(patientRepository, never())
                    .save(any(Patient.class));
        }


        @Test
        void shouldThrowAlreadyExistWhenPhoneAlreadyExists() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.of(patient));

            when(patientRepository.existsByPhoneAndIdNot(
                    patientUpdateRequestDto.phone(),
                    1L
            )).thenReturn(true);
            AlreadyExistException exception =
                    assertThrows(
                            AlreadyExistException.class,
                            () -> patientService.update(
                                    1L,
                                    patientUpdateRequestDto
                            )
                    );

            assertEquals(
                    "Patient with this phone number already exists",
                    exception.getMessage()
            );

            verify(patientRepository)
                    .findById(1L);

            verify(patientRepository)
                    .existsByPhoneAndIdNot(
                            patientUpdateRequestDto.phone(),
                            1L
                    );

            verify(patientMapper, never())
                    .updateEntity(
                            any(PatientUpdateRequestDto.class),
                            any(Patient.class)
                    );

            verify(patientRepository, never())
                    .save(any(Patient.class));
        }
    }

    @Nested
    class DeletePatient {

        @Test
        void shouldDeletePatientSuccessfully() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.of(patient));

            patientService.delete(1L);

            verify(patientRepository)
                    .findById(1L);

            verify(patientRepository)
                    .delete(patient);
        }


        @Test
        void shouldThrowResourceNotFoundWhenDeletingNonExistingPatient() {

            when(patientRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResourceNotFound exception =
                    assertThrows(
                            ResourceNotFound.class,
                            () -> patientService.delete(1L)
                    );

            assertEquals(
                    "Patient Not Found",
                    exception.getMessage()
            );

            verify(patientRepository)
                    .findById(1L);

            verify(patientRepository, never())
                    .delete(any(Patient.class));
        }
    }
}