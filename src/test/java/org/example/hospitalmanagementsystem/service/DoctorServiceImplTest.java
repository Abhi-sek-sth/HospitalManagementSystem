package org.example.hospitalmanagementsystem.service;


import org.example.hospitalmanagementsystem.dto.request.DoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.request.UpdateDoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;
import org.example.hospitalmanagementsystem.entity.Department;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.mapper.DoctorMapper;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {
    @Mock
    private  DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private RegisterRepository registerRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Register register;
    private Department department;
    private DoctorRequestDto doctorRequestDto;
    private DoctorResponseDto doctorResponseDto;
    private Doctor doctor;
    private UpdateDoctorRequestDto updateDoctorRequestDto;

    @BeforeEach
    void setUp(){
        doctorRequestDto = DoctorRequestDto.builder()
                .phone("9813888888")
                .departmentId(1L)
                .licenseNumber("12345")
                .userId(1L)
                .specialization("Cardiology")
                .active(true)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .licenseNumber("12345")
                .phone("1234567890")
                .user(register)
                .department(department)
                .specialization("Cardiology")
                .active(true)
                .createdAt(Instant.now())
                .build();

        doctorResponseDto = DoctorResponseDto.builder()
                .id(1L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .phone("9813888888")
                .departmentId(1L)
                .licenseNumber("12345")
                .userId(1L)
                .specialization("Cardiology")
                .active(true)
                .build();

        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .location("4th Floor")
                .description("Heart Department")
                .createdAt(LocalDateTime.now())
                .build();

        register = Register.builder()
                .id(1L)
                .email("abcd@gmail.com")
                .role("Admin")
                .firstName("ram")
                .lastName("Gurung")
                .username("ram")
                .password("jdkdfkfjk")
                .createdAt(LocalDateTime.now())
                .build();

        updateDoctorRequestDto = UpdateDoctorRequestDto.builder()
                .departmentId(2L)
                .phone("1234567890")
                .specialization("Neurology")
                .active(true)
                .build();
    }

    @Nested
    class CreateDoctor{
        @Test
        @DisplayName("should create doctor successfully")
        void shouldCreateDoctorSuccessfully(){
            when(doctorRepository.existsByLicenseNumber(doctorRequestDto.licenseNumber())).thenReturn(false);
            when(doctorRepository.existsByUserId(doctorRequestDto.userId())).thenReturn(false);
            when(registerRepository.findById(doctorRequestDto.userId())).thenReturn(Optional.of(register));
            when(departmentRepository.findById(doctorRequestDto.departmentId())).thenReturn(Optional.of(department));
            when(doctorMapper.toEntity(doctorRequestDto)).thenReturn(doctor);
            when(doctorRepository.save(doctor)).thenReturn(doctor);
            when(doctorMapper.toResponseDto(doctor)).thenReturn(doctorResponseDto);

            DoctorResponseDto responseDto = doctorService.create(doctorRequestDto);

           assertEquals(doctorResponseDto,responseDto);

           verify(doctorRepository).existsByLicenseNumber(doctorRequestDto.licenseNumber());
           verify(doctorRepository).existsByUserId(doctorRequestDto.userId());
           verify(registerRepository).findById(doctorRequestDto.userId());
           verify(departmentRepository).findById(doctorRequestDto.departmentId());
           verify(doctorMapper).toEntity(doctorRequestDto);
           verify(doctorRepository).save(doctor);
           verify(doctorMapper).toResponseDto(doctor);



        }
        @Test
        @DisplayName("should throw ResourceNotFound")
        void shouldThrowResourceNotFoundWhenUserNotFound(){
            when(registerRepository.findById(doctorRequestDto.userId())).thenReturn(Optional.empty());


            ResourceNotFound result = assertThrows(ResourceNotFound.class,() -> doctorService.create(doctorRequestDto));

            assertEquals("User Not Found",result.getMessage());


            verify(registerRepository).findById(doctorRequestDto.userId());


            verify(departmentRepository, never()).findById(any());


            verify(doctorRepository,never()).save(any());

            verifyNoMoreInteractions(doctorMapper);


        }
        @Test
        @DisplayName("should throw ResourceNotFound when department does not exist")
        void shouldThrowResourceNotFoundWhenDepartmentNotFound() {

            when(registerRepository.findById(doctorRequestDto.userId()))
                    .thenReturn(Optional.of(register));

            when(departmentRepository.findById(doctorRequestDto.departmentId()))
                    .thenReturn(Optional.empty());

            ResourceNotFound result = assertThrows(
                    ResourceNotFound.class,
                    () -> doctorService.create(doctorRequestDto)
            );

            assertEquals("Department Not Found", result.getMessage());

            verify(registerRepository)
                    .findById(doctorRequestDto.userId());

            verify(departmentRepository)
                    .findById(doctorRequestDto.departmentId());

            verify(doctorRepository, never())
                    .save(any());

            verifyNoInteractions(doctorMapper);
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExists when license number already exists")
        void shouldThrowResourceAlreadyExistsWhenLicenseNumberAlreadyExists() {

            // Arrange
            when(doctorRepository.existsByLicenseNumber(
                    doctorRequestDto.licenseNumber()
            )).thenReturn(true);

            // Act
            AlreadyExistException exception = assertThrows(
                    AlreadyExistException.class,
                    () -> doctorService.create(doctorRequestDto)
            );

            // Assert
            assertEquals("License Number already exists", exception.getMessage());

            verify(doctorRepository)
                    .existsByLicenseNumber(doctorRequestDto.licenseNumber());

            verify(doctorRepository, never())
                    .existsByUserId(any());

            verify(registerRepository, never())
                    .findById(any());

            verify(departmentRepository, never())
                    .findById(any());

            verify(doctorRepository, never())
                    .save(any());

            verifyNoInteractions(doctorMapper);
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExists when user already exists")
        void shouldThrowResourceAlreadyExistsWhenUserAlreadyExists() {

            // Arrange
            when(doctorRepository.existsByLicenseNumber(
                    doctorRequestDto.licenseNumber()
            )).thenReturn(false);

            when(doctorRepository.existsByUserId(
                    doctorRequestDto.userId()
            )).thenReturn(true);

            // Act
            AlreadyExistException exception = assertThrows(
                    AlreadyExistException.class,
                    () -> doctorService.create(doctorRequestDto)
            );

            // Assert
            assertEquals("User id already register as a doctor", exception.getMessage());

            verify(doctorRepository)
                    .existsByLicenseNumber(doctorRequestDto.licenseNumber());

            verify(doctorRepository)
                    .existsByUserId(doctorRequestDto.userId());

            verify(registerRepository, never())
                    .findById(any());

            verify(departmentRepository, never())
                    .findById(any());

            verify(doctorRepository, never())
                    .save(any());

            verifyNoInteractions(doctorMapper);
        }

    }
    @Nested
    class GetDoctorById{
        @Test
        @DisplayName("should return doctor when doctor exists")
        void shouldReturnDoctorWhenDoctorExists() {

            // Arrange
            Long doctorId = 1L;

            when(doctorRepository.findById(doctorId))
                    .thenReturn(Optional.of(doctor));

            when(doctorMapper.toResponseDto(doctor))
                    .thenReturn(doctorResponseDto);

            // Act
            DoctorResponseDto result = doctorService.getById(doctorId);

            // Assert
            assertEquals(doctorResponseDto, result);

            verify(doctorRepository)
                    .findById(doctorId);

            verify(doctorMapper)
                    .toResponseDto(doctor);
        }

        @Test
        @DisplayName("should throw ResourceNotFound when doctor does not exist")
        void shouldThrowResourceNotFoundWhenDoctorDoesNotExist() {

            // Arrange
            Long doctorId = 1L;

            when(doctorRepository.findById(doctorId))
                    .thenReturn(Optional.empty());

            // Act
            ResourceNotFound exception = assertThrows(
                    ResourceNotFound.class,
                    () -> doctorService.getById(doctorId)
            );

            // Assert
            assertEquals("Doctor Not Found", exception.getMessage());

            verify(doctorRepository)
                    .findById(doctorId);

            verifyNoInteractions(doctorMapper);
        }
        @Nested
        class UpdateDoctor{
            @Test
            void shouldUpdateDoctorWhenDoctorExists(){
                when(doctorRepository.findById(1L))
                        .thenReturn(Optional.of(doctor));

                when(departmentRepository.findById(2L))
                        .thenReturn(Optional.of(department));

                when(doctorRepository.save(doctor))
                        .thenReturn(doctor);

                when(doctorMapper.toResponseDto(doctor))
                        .thenReturn(doctorResponseDto);

                DoctorResponseDto result = doctorService.update(1L, updateDoctorRequestDto);

                assertEquals(doctorResponseDto, result);

                assertEquals(updateDoctorRequestDto.active(), doctor.getActive());
                assertEquals(updateDoctorRequestDto.specialization(), doctor.getSpecialization());
                assertEquals(updateDoctorRequestDto.phone(), doctor.getPhone());
                assertEquals(department, doctor.getDepartment());

                verify(doctorRepository).findById(1L);
                verify(departmentRepository).findById(2L);
                verify(doctorRepository).save(doctor);
                verify(doctorMapper).toResponseDto(doctor);
            }

            @Test
            @DisplayName("should throw ResourceNotFound when doctor does not exist")
            void shouldThrowResourceNotFoundWhenDoctorDoesNotExist() {

                // ArrangeLong

                when(doctorRepository.findById(1L))
                        .thenReturn(Optional.empty());

                // Act
                ResourceNotFound exception = assertThrows(
                        ResourceNotFound.class,
                        () -> doctorService.update(1L,updateDoctorRequestDto)
                );

                // Assert
                assertEquals("Doctor Not Found", exception.getMessage());

                verify(doctorRepository)
                        .findById(1L);

                verifyNoInteractions(doctorMapper);
            }

            @Test
            @DisplayName("should throw ResourceNotFound when department does not exist")
            void shouldThrowResourceNotFoundWhenDepartmentDoesNotExist() {

                // ArrangeLong
                when(doctorRepository.findById(1L))
                        .thenReturn(Optional.of(doctor));

                when(departmentRepository.findById(2L))
                        .thenReturn(Optional.empty());

                // Act
                ResourceNotFound exception = assertThrows(
                        ResourceNotFound.class,
                        () -> doctorService.update(1L,updateDoctorRequestDto)
                );

                // Assert
                assertEquals("Department Not Found", exception.getMessage());

                verify(doctorRepository).findById(1L);
                verify(departmentRepository)
                        .findById(2L);

                verifyNoInteractions(doctorMapper);
            }
            @Nested
            class DeleteDoctor{
                @Test
                void shouldDeleteDoctor(){
                    when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

                    doctorService.delete(1L);

                    verify(doctorRepository).findById(1L);
                    verify(doctorRepository).delete(doctor);

                }
                @Test
                @DisplayName("should throw resource not found exception")
                void shouldThrowResourceNotFoundExceptionWhenDoctorNotFound(){
                    when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

                    ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> doctorService.delete(1L));

                    assertEquals("Doctor Not Found",exception.getMessage());

                    verify(doctorRepository).findById(1L);
                }
            }

            @Nested
            @DisplayName("should display all doctors")
            class getAllDoctors{
                @Test
                void shouldGetAllDoctors(){
                    Page<Doctor> doctorPage = new PageImpl<>(List.of(doctor), PageRequest.of(0,1),1);
                    when(doctorRepository.findAll(any(Pageable.class)))
                            .thenReturn(doctorPage);

                    when(doctorMapper.toResponseDto(doctor))
                            .thenReturn(doctorResponseDto);



                    DoctorPageResponseDto<DoctorResponseDto> result =
                            doctorService.getAll(0,1);

                    assertEquals(1, result.totalElements());
                    assertEquals(1, result.totalPages());
                    assertEquals(0, result.currentPage());

                    assertEquals(1, result.content().size());
                    assertEquals(doctorResponseDto, result.content().get(0));


                    verify(doctorRepository).findAll(any(Pageable.class));
                    verify(doctorMapper).toResponseDto(doctor);


                    verifyNoMoreInteractions(
                            doctorRepository,
                            doctorMapper
                    );
                }
            }

        }
    }

}