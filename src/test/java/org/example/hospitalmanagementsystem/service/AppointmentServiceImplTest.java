package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.AppointmentRequestDto;
import org.example.hospitalmanagementsystem.dto.request.AppointmentUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.AppointmentResponseDto;
import org.example.hospitalmanagementsystem.entity.*;
import org.example.hospitalmanagementsystem.exception.*;
import org.example.hospitalmanagementsystem.mapper.AppointmentMapper;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.example.hospitalmanagementsystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Test")
public class AppointmentServiceImplTest {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private CurrentUserServiceImpl currentUserService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment;
    private AppointmentRequestDto appointmentRequestDto;
    private AppointmentRequestDto appointmentRequestDto2;
    private AppointmentUpdateRequestDto appointmentUpdateRequestDto;
    private AppointmentResponseDto appointmentResponseDto;
    private Patient patient;
    private Register register;
    private Doctor doctor;
    private Department department;


    @BeforeEach
    void setUp(){
        appointmentRequestDto = new AppointmentRequestDto(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                "Fever and headache",
                "Patient has had symptoms for two days"
        );

         appointmentRequestDto2 = new AppointmentRequestDto(
                1L,
                1L,
                LocalDate.now(),
                LocalTime.now().minusMinutes(10),
                "Fever and headache",
                "Patient has had symptoms for two days"
        );

        register = Register.builder()
                .id(1L)
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
                .user(register)
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
                .user(register)
                .department(department)
                .specialization("Cardiology")
                .active(true)
                .createdAt(Instant.now())
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(appointmentRequestDto.appointmentDate())
                .appointmentTime(appointmentRequestDto.appointmentTime())
                .reason(appointmentRequestDto.reason())
                .notes(appointmentRequestDto.notes())
                .status(AppointmentStatus.PENDING)
                .build();


        appointmentResponseDto = new AppointmentResponseDto(
                1L,
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                "Fever and headache",
                AppointmentStatus.PENDING,
                "Patient has had symptoms for two days",
                Instant.now(),
                Instant.now()
        );
        appointmentUpdateRequestDto = new AppointmentUpdateRequestDto(
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 30),
                "Fever and headache",
                AppointmentStatus.PENDING,
                "Patient has had symptoms for two days"
        );

    }
    @Nested
    @DisplayName("Create Appointment")
    class CreateAppointment{
        @Test
        @DisplayName("Should create Appointment")
        void shouldCreateAppointment(){
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(appointmentRequestDto.doctorId())).thenReturn(Optional.of(doctor));
            when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime())).thenReturn(false);
            when(appointmentMapper.toEntity(appointmentRequestDto)).thenReturn(appointment);
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentResponseDto appointmentResponseDto1 = appointmentService.create(appointmentRequestDto);

            assertEquals(appointmentResponseDto,appointmentResponseDto1);

            verify(patientRepository).findById(appointmentRequestDto.patientId());
            verify(doctorRepository).findById(appointmentRequestDto.doctorId());
            verify(appointmentRepository).existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime());
            verify(appointmentMapper).toEntity(appointmentRequestDto);
            verify(appointmentRepository).save(appointment);
            verify(appointmentMapper).toResponseDto(appointment);

        }
        @Test
        @DisplayName("should throw TimeAlreadyFinishException")
        void timeAlreadyFinishException(){
            TimeAlreadyFinishException exception = assertThrows(TimeAlreadyFinishException.class, () -> appointmentService.create(appointmentRequestDto2));

            assertEquals(
                    "Appointment date and time cannot be in the past", exception.getMessage());

            verifyNoInteractions(
                    patientRepository,
                    doctorRepository,
                    appointmentRepository,
                    appointmentMapper
            );
        }
        @Test
        @DisplayName("should throw ResourceNotFound exception")
        void patientNotFound(){
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.empty());

            ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class,() -> appointmentService.create(appointmentRequestDto));

            assertEquals("Patient Not Found",resourceNotFound.getMessage());

            verify(patientRepository)
                    .findById(appointmentRequestDto.patientId());

            verifyNoInteractions(
                    doctorRepository,
                    appointmentRepository,
                    appointmentMapper
            );

        }

        @Test
        @DisplayName("should throw doctor not found")
        void doctorNotFound(){
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.of(patient));

            when(doctorRepository.findById(appointmentRequestDto.doctorId())).thenReturn(Optional.empty());

            ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class,() -> appointmentService.create(appointmentRequestDto));

            assertEquals("Doctor Not Found",resourceNotFound.getMessage());

            verify(doctorRepository)
                    .findById(appointmentRequestDto.patientId());

            verifyNoInteractions(
                    appointmentRepository,
                    appointmentMapper
            );
        }
        @Test
        @DisplayName("should throw doctorInactiveException")
        void doctorInactiveException(){
            doctor.setActive(false);
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(appointmentRequestDto.doctorId())).thenReturn(Optional.of(doctor));

            DoctorInactiveException exception = assertThrows(DoctorInactiveException.class,() -> appointmentService.create(appointmentRequestDto));

            assertEquals("Doctor is currently inactive",exception.getMessage());

            verify(patientRepository).findById(appointmentRequestDto.patientId());
            verify(doctorRepository).findById(appointmentRequestDto.doctorId());

            verifyNoInteractions(
                    appointmentMapper,
                    appointmentRepository
            );
        }
        @Test
        @DisplayName("should throw AlreadyExistsException")
        void alreadyExistsExceptionBeforeSave(){
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(appointmentRequestDto.doctorId())).thenReturn(Optional.of(doctor));
            when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime())).thenReturn(true);

            AlreadyExistException alreadyExistException = assertThrows(AlreadyExistException.class,() -> appointmentService.create(appointmentRequestDto));

            assertEquals("Doctor already has an appointment at this time",alreadyExistException.getMessage());

            verify(patientRepository).findById(appointmentRequestDto.patientId());
            verify(doctorRepository).findById(appointmentRequestDto.doctorId());
            verify(appointmentRepository).existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime());

            verifyNoInteractions(
                    appointmentMapper
            );
        }
        @Test
        @DisplayName("should throw AlreadyExistsException while saving")
        void alreadyExistsExceptionWhileSaving(){
            when(patientRepository.findById(appointmentRequestDto.patientId())).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(appointmentRequestDto.doctorId())).thenReturn(Optional.of(doctor));
            when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime())).thenReturn(false);
            when(appointmentMapper.toEntity(appointmentRequestDto)).thenReturn(appointment);
            when(appointmentRepository.save(appointment)).thenThrow(new DataIntegrityViolationException("Duplicate appointment"));

            AlreadyExistException alreadyExistException = assertThrows(AlreadyExistException.class,() -> appointmentService.create(appointmentRequestDto));

            assertEquals("Doctor already has an appointment at this time",alreadyExistException.getMessage());

            verify(patientRepository).findById(appointmentRequestDto.patientId());
            verify(doctorRepository).findById(appointmentRequestDto.doctorId());
            verify(appointmentRepository).existsByDoctorIdAndAppointmentDateAndAppointmentTime(appointmentRequestDto.doctorId(),appointmentRequestDto.appointmentDate(),appointmentRequestDto.appointmentTime());
            verify(appointmentMapper).toEntity(appointmentRequestDto);
            verify(appointmentRepository).save(appointment);
        }
    }
    @Nested
    @DisplayName("Get Appointment By Id")
    class GetByIdAppointment{
        @Test
        @DisplayName("Should Get Appointment with the help of id Admin")
        void shouldGetAppointmentByIdByAdmin(){
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isAdmin()).thenReturn(true);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentResponseDto responseDto = appointmentService.getById(1L);

            assertEquals(appointmentResponseDto,responseDto);

            verify(appointmentRepository).findById(1L);
            verify(appointmentMapper).toResponseDto(appointment);
        }

        @Test
        @DisplayName("Should Get Appointment with the help of id by Receptionist")
        void shouldGetAppointmentByIdByReceptionist(){
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isReceptionist()).thenReturn(true);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentResponseDto responseDto = appointmentService.getById(1L);

            assertEquals(appointmentResponseDto,responseDto);

            verify(appointmentRepository).findById(1L);
            verify(appointmentMapper).toResponseDto(appointment);
        }
        @Test
        @DisplayName("Should Get Appointment of own with the help of id by doctor")
        void shouldGetAppointmentById_whenDoctorIsOwner(){
            Long doctorUserId = 1L;

            Register register = new Register();
            register.setId(1L);

            Doctor doctor = new Doctor();
            doctor.setUser(register);

            appointment.setDoctor(doctor);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);
            when(currentUserService.getCurrentUserId()).thenReturn(doctorUserId);
            when(currentUserService.isDoctor()).thenReturn(true);


            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentResponseDto responseDto = appointmentService.getById(1L);

            assertEquals(appointmentResponseDto,responseDto);

            verify(appointmentRepository).findById(1L);
            verify(appointmentMapper).toResponseDto(appointment);
        }
        @Test
        @DisplayName("Should Get Appointment of own with the help of id by Patient")
        void shouldGetAppointmentById_whenPatientIsOwner(){
            Long patientUserId = 1L;

            Register register = new Register();
            register.setId(1L);

            Patient patient = new Patient();
            patient.setUser(register);

            appointment.setPatient(patient);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);
            when(currentUserService.getCurrentUserId()).thenReturn(patientUserId);
            when(currentUserService.isDoctor()).thenReturn(false);
            when(currentUserService.isPatient()).thenReturn(true);

            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentResponseDto responseDto = appointmentService.getById(1L);

            assertEquals(appointmentResponseDto,responseDto);

            verify(appointmentRepository).findById(1L);
            verify(appointmentMapper).toResponseDto(appointment);
        }
        @Test
        @DisplayName("should throw AccessDeniedExceptionHandler when doctor is not owner")
        void getById_shouldThrowAccessDenied_whenDoctorIsNotOwner(){
            Long doctorUserId = 1L;

            Register register = new Register();
            register.setId(2L);

            Doctor doctor = new Doctor();
            doctor.setUser(register);

            appointment.setDoctor(doctor);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);
            when(currentUserService.getCurrentUserId()).thenReturn(doctorUserId);
            when(currentUserService.isDoctor()).thenReturn(true);

            AccessDeniedExceptionHandler exceptionHandler = assertThrows(AccessDeniedExceptionHandler.class,() -> appointmentService.getById(1L));

            assertEquals("You are not allowed to access this appointment",exceptionHandler.getMessage());

            verify(appointmentRepository).findById(1L);
            verifyNoInteractions(appointmentMapper);
        }
        @Test
        @DisplayName("should throw AccessDeniedExceptionHandler when doctor is not owner")
        void getById_shouldThrowAccessDenied_whenPatientIsNotOwner(){
            Long patientUserId = 1L;

            Register register = new Register();
            register.setId(2L);

            Patient patient = new Patient();
            patient.setUser(register);

            appointment.setPatient(patient);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);
            when(currentUserService.getCurrentUserId()).thenReturn(patientUserId);
            when(currentUserService.isDoctor()).thenReturn(false);
            when(currentUserService.isPatient()).thenReturn(true);

            AccessDeniedExceptionHandler exceptionHandler = assertThrows(AccessDeniedExceptionHandler.class,() -> appointmentService.getById(1L));

            assertEquals("You are not allowed to access this appointment",exceptionHandler.getMessage());

            verify(appointmentRepository).findById(1L);
            verifyNoInteractions(appointmentMapper);
        }
        @Test
        @DisplayName("Should Throw Resource Not Found Exception")
        void shouldThrowResourceNotFoundException(){
            when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class,() -> appointmentService.getById(1L));

            assertEquals("Appointment Not Found",resourceNotFound.getMessage());

            verify(appointmentRepository).findById(1L);
            verifyNoInteractions(appointmentMapper);
        }
        @Test
        @DisplayName("should throw AccessDeniedExceptionHandler when user is neither doctor nor patient")
        void getById_shouldThrowAccessDenied_whenUserHasNoValidRole() {
            Long appointmentId = 1L;

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);
            when(currentUserService.getCurrentUserId()).thenReturn(50L);
            when(currentUserService.isDoctor()).thenReturn(false);
            when(currentUserService.isPatient()).thenReturn(false);

            AccessDeniedExceptionHandler accessDeniedExceptionHandler = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.getById(appointmentId));
            assertEquals("You are not allowed to access this appointment",accessDeniedExceptionHandler.getMessage());
            verify(appointmentMapper, never()).toResponseDto(any());
        }
    @Nested
    @DisplayName("Should get all appointments")
     class getAllAppointments{
            @Test
            @DisplayName("Should Retrieve All Appointments if user is admin")
            void shouldRetrieveAllAppointments_whenUserHasAdminRole(){
                when(currentUserService.isAdmin()).thenReturn(true);
                Page<Appointment> appointmentPageList = new PageImpl<>(List.of(appointment), PageRequest.of(0,5),1);
                when(appointmentRepository.findAll(any(Pageable.class))).thenReturn(appointmentPageList);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                AppointmentPageResponseDto<AppointmentResponseDto> result = appointmentService.getAll(0,5);

                assertEquals(1, result.totalElements());
                assertEquals(1, result.totalPages());
                assertEquals(0, result.currentPage());

                assertEquals(1, result.content().size());
                assertEquals(appointmentResponseDto, result.content().get(0));

                verify(appointmentRepository).findAll(any(Pageable.class));
                verify(appointmentMapper).toResponseDto(appointment);

            }
        @Test
        @DisplayName("Should Retrieve All Appointments if user is Receptionist")
        void shouldRetrieveAllAppointments_whenUserHasReceptionistRole(){
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(true);
            Page<Appointment> appointmentPageList = new PageImpl<>(List.of(appointment), PageRequest.of(0,5),1);
            when(appointmentRepository.findAll(any(Pageable.class))).thenReturn(appointmentPageList);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

            AppointmentPageResponseDto<AppointmentResponseDto> result = appointmentService.getAll(0,5);

            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals(0, result.currentPage());

            assertEquals(1, result.content().size());
            assertEquals(appointmentResponseDto, result.content().get(0));

            verify(appointmentRepository).findAll(any(Pageable.class));
            verify(appointmentMapper).toResponseDto(appointment);

        }
        @Test
        @DisplayName("Should not Retrieve All Appointments if user is not Admin orReceptionist")
        void shouldNotRetrieveAppointmentsExceptAdminAndReceptionist(){
            when(currentUserService.isAdmin()).thenReturn(false);
            when(currentUserService.isReceptionist()).thenReturn(false);

            AccessDeniedExceptionHandler accessDeniedExceptionHandler = assertThrows(AccessDeniedExceptionHandler.class,() -> appointmentService.getAll(0, 1));

           assertEquals("You are not allowed to access all appointments",accessDeniedExceptionHandler.getMessage());

           verifyNoInteractions(appointmentMapper,appointmentRepository);

        }
        @Nested
        @DisplayName("Update Appointment")
        class UpdateAppointment{
                @Test
                @DisplayName("Should update appointment by admin")
                void shouldUpdateAppointmentByAdmin(){
                    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                    when(currentUserService.isAdmin()).thenReturn(true);
                    when(appointmentRepository.save(appointment)).thenReturn(appointment);
                    when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                    AppointmentResponseDto result = appointmentService.update(1L, appointmentUpdateRequestDto);

                    assertNotNull(result);

                    verify(appointmentRepository).findById(1L);
                    verify(appointmentRepository).save(appointment);
                    verify(appointmentMapper).toResponseDto(appointment);

                }

            @Test
            @DisplayName("Should update appointment by Receptionist")
            void shouldUpdateAppointmentByReceptionist(){
                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(true);
                when(appointmentRepository.save(appointment)).thenReturn(appointment);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                AppointmentResponseDto result = appointmentService.update(1L, appointmentUpdateRequestDto);

                assertNotNull(result);

                verify(appointmentRepository).findById(1L);
                verify(appointmentRepository).save(appointment);
                verify(appointmentMapper).toResponseDto(appointment);

            }
            @Test
            @DisplayName("Should update own appointment when user is Patient")
            void shouldUpdateOwnAppointmentByPatient(){
                    Long patientUserId = 1L;

                    Register register = new Register();
                    register.setId(1L);

                    Patient patient = new Patient();
                    patient.setUser(register);

                    appointment.setPatient(patient);
                    appointment.setStatus(AppointmentStatus.PENDING);


                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(patientUserId);
                when(appointmentRepository.save(appointment)).thenReturn(appointment);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                AppointmentResponseDto result = appointmentService.update(1L, appointmentUpdateRequestDto);

                assertNotNull(result);

                verify(appointmentRepository).findById(1L);
                verify(appointmentRepository).save(appointment);
                verify(appointmentMapper).toResponseDto(appointment);

            }
            @Test
            @DisplayName("Should update own appointment when user is doctor")
            void shouldUpdateOwnAppointmentByDoctor() {

                Register doctorUser = new Register();
                doctorUser.setId(3L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);
                doctor.setActive(true);

                appointment.setDoctor(doctor);
                appointment.setStatus(AppointmentStatus.PENDING);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(false);
                when(currentUserService.isDoctor()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(3L);

                when(appointmentRepository.save(appointment)).thenReturn(appointment);

                when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                AppointmentResponseDto result = appointmentService.update(1L, appointmentUpdateRequestDto);

                assertNotNull(result);

                verify(appointmentRepository).save(appointment);
            }
            @Test
            @DisplayName("Should throw AccessDeniedException when user is not appointment owner")
            void shouldThrowAccessDeniedWhenUserIsNotOwner() {

                Register patientUser = new Register();
                patientUser.setId(3L);

                Patient patient = new Patient();
                patient.setUser(patientUser);


                appointment.setPatient(patient);
                appointment.setStatus(AppointmentStatus.PENDING);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(4L);

                AccessDeniedExceptionHandler accessDeniedExceptionHandler = assertThrows(AccessDeniedExceptionHandler.class,() -> appointmentService.update(1L,appointmentUpdateRequestDto));

                assertEquals("You are not allowed to update this appointment",accessDeniedExceptionHandler.getMessage());

                verify(appointmentRepository).findById(1L);
                verify(appointmentRepository, never()).save(any());
                verifyNoInteractions(appointmentMapper);
            }
            @Test
            @DisplayName("Should throw AccessDeniedException when user is not appointment owner")
            void shouldThrowAccessDeniedWhenUserIsNotOwner2() {

                Register doctorUser = new Register();
                doctorUser.setId(3L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);


                appointment.setDoctor(doctor);
                appointment.setStatus(AppointmentStatus.PENDING);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(false);
                when(currentUserService.isDoctor()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(4L);

                AccessDeniedExceptionHandler accessDeniedExceptionHandler = assertThrows(AccessDeniedExceptionHandler.class,() -> appointmentService.update(1L,appointmentUpdateRequestDto));

                assertEquals("You are not allowed to update this appointment",accessDeniedExceptionHandler.getMessage());

                verify(appointmentRepository).findById(1L);
                verify(appointmentRepository, never()).save(any());
                verifyNoInteractions(appointmentMapper);
            }
            @Test
            @DisplayName("Should throw InvalidOperationException when appointment is not pending")
            void shouldThrowInvalidOperationWhenAppointmentIsNotPending() {

                Register patientUser = new Register();
                patientUser.setId(2L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                appointment.setPatient(patient);
                appointment.setStatus(AppointmentStatus.CONFIRMED);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(2L);

                assertThrows(InvalidOperationException.class, () -> appointmentService.update(1L, appointmentUpdateRequestDto));

                verify(appointmentRepository, never()).save(any());
            }
            @Test
            @DisplayName("Should throw DoctorInactiveException when changing time for inactive doctor")
            void shouldThrowDoctorInactiveExceptionWhenChangingTime() {
                Register userDoctor = new Register();
                userDoctor.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(userDoctor);
                doctor.setActive(false);

                appointment.setDoctor(doctor);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isDoctor()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(1L);

                DoctorInactiveException doctorInactiveException = assertThrows(DoctorInactiveException.class,() -> appointmentService.update(1L,appointmentUpdateRequestDto));
                assertEquals("Doctor is currently inactive",doctorInactiveException.getMessage());
                verify(appointmentRepository, never()).save(any());
        }
            @Test
            @DisplayName("Should throw TimeAlreadyFinishException when new appointment time is in the past")
            void shouldThrowTimeAlreadyFinishExceptionWhenNewTimeIsPast() {

                Doctor doctor = new Doctor();
                doctor.setId(10L);
                doctor.setActive(true);

                appointment.setDoctor(doctor);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(true);

                AppointmentUpdateRequestDto pastRequest = new AppointmentUpdateRequestDto(
                                LocalDate.now(),
                                LocalTime.now().minusHours(1),
                                "Updated reason",
                                AppointmentStatus.PENDING,
                                "Updated notes"
                        );

                assertThrows(TimeAlreadyFinishException.class, () -> appointmentService.update(1L, pastRequest));

                verify(appointmentRepository, never()).save(any());
            }
            @Test
            @DisplayName("Should throw AlreadyExistException when new appointment time is already booked")
            void shouldThrowAlreadyExistExceptionWhenNewTimeIsBooked() {

                Doctor doctor = new Doctor();
                doctor.setId(10L);
                doctor.setActive(true);

                appointment.setDoctor(doctor);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(true);

                when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        eq(10L),
                        eq(appointmentUpdateRequestDto.appointmentDate()),
                        eq(appointmentUpdateRequestDto.appointmentTime())
                )).thenReturn(true);

                assertThrows(AlreadyExistException.class, () -> appointmentService.update(1L, appointmentUpdateRequestDto));

                verify(appointmentRepository, never()).save(any());
            }
            @Test
            @DisplayName("Should update appointment when new time is available")
            void shouldUpdateAppointmentWhenTimeIsChanged() {

                Doctor doctor = new Doctor();
                doctor.setId(10L);
                doctor.setActive(true);

                appointment.setDoctor(doctor);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

                when(currentUserService.isAdmin()).thenReturn(true);


                when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        eq(10L),
                        eq(appointmentUpdateRequestDto.appointmentDate()),
                        eq(appointmentUpdateRequestDto.appointmentTime())
                )).thenReturn(false);

                when(appointmentRepository.save(appointment)).thenReturn(appointment);

                when(appointmentMapper.toResponseDto(appointment)).thenReturn(appointmentResponseDto);

                AppointmentResponseDto result = appointmentService.update(1L, appointmentUpdateRequestDto);

                assertNotNull(result);

                verify(appointmentRepository).existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        eq(10L),
                        eq(appointmentUpdateRequestDto.appointmentDate()),
                        eq(appointmentUpdateRequestDto.appointmentTime())
                );

                verify(appointmentRepository).save(appointment);
                verify(appointmentMapper).toResponseDto(appointment);
            }
            @Test
            @DisplayName("Should throw ResourceNotFound when appointment does not exist")
            void shouldThrowResourceNotFoundWhenAppointmentDoesNotExist() {

                when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> appointmentService.update(1L, appointmentUpdateRequestDto));

                verify(appointmentRepository, never()).save(any());
            }
    }
        @Nested
        @DisplayName("Delete Appointment")
        class DeleteAppointment {

            @Test
            @DisplayName("Should throw ResourceNotFound when appointment does not exist")
            void shouldThrowResourceNotFoundWhenAppointmentDoesNotExist() {
                when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

                ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class,
                        () -> appointmentService.delete(1L));

                assertEquals("Appointment not found", resourceNotFound.getMessage());
                verify(appointmentRepository, never()).delete(any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when user is not admin or receptionist")
            void shouldThrowAccessDeniedWhenUserIsNotStaff() {
                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.delete(1L));

                assertEquals("You are not allowed to delete this appointment", exception.getMessage());
                verify(appointmentRepository, never()).delete(any());
            }

            @Test
            @DisplayName("Should throw InvalidOperationException when appointment is already completed")
            void shouldThrowInvalidOperationExceptionWhenAppointmentIsCompleted() {
                appointment.setStatus(AppointmentStatus.COMPLETED);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(true);

                InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> appointmentService.delete(1L));

                assertEquals("Cannot delete a completed appointment", exception.getMessage());
                verify(appointmentRepository, never()).delete(any());
            }

            @Test
            @DisplayName("Should delete appointment successfully when user is admin")
            void shouldDeleteAppointmentSuccessfullyWhenUserIsAdmin() {
                appointment.setStatus(AppointmentStatus.PENDING);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(true);


                appointmentService.delete(1L);

                verify(appointmentRepository, times(1)).delete(appointment);
            }

            @Test
            @DisplayName("Should delete appointment successfully when user is receptionist")
            void shouldDeleteAppointmentSuccessfullyWhenUserIsReceptionist() {
                appointment.setStatus(AppointmentStatus.CONFIRMED);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(true);

                appointmentService.delete(1L);

                verify(appointmentRepository, times(1)).delete(appointment);
            }
        }
        @Nested
        @DisplayName("Get Appointments By Patient")
        class GetByPatientAppointment {

            @Test
            @DisplayName("Should throw ResourceNotFound when patient does not exist")
            void shouldThrowResourceNotFoundWhenPatientDoesNotExist() {
                Pageable pageable = PageRequest.of(0, 10);

                when(patientRepository.findById(1L)).thenReturn(Optional.empty());

                ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class, () -> appointmentService.getByPatient(1L, pageable));

                assertEquals("Patient not found", resourceNotFound.getMessage());
                verify(appointmentRepository, never()).findByPatientId(any(), any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when user is neither staff nor patient")
            void shouldThrowAccessDeniedWhenUserIsNeitherStaffNorPatient() {
                Pageable pageable = PageRequest.of(0, 10);

                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(false);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.getByPatient(1L, pageable));

                assertEquals("You are not allowed to access patient appointments", exception.getMessage());
                verify(appointmentRepository, never()).findByPatientId(any(), any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when patient tries to access another patient's appointments")
            void shouldThrowAccessDeniedWhenPatientAccessesAnotherPatientsAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(2L);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.getByPatient(1L, pageable));

                assertEquals("You are not allowed to access another patient's appointments", exception.getMessage());
                verify(appointmentRepository, never()).findByPatientId(any(), any());
            }

            @Test
            @DisplayName("Should return appointments when patient accesses their own appointments")
            void shouldReturnAppointmentsWhenPatientAccessesOwnAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(1L);
                when(appointmentRepository.findByPatientId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByPatient(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByPatientId(1L, pageable);
            }

            @Test
            @DisplayName("Should return appointments when staff (admin) accesses patient appointments")
            void shouldReturnAppointmentsWhenAdminAccessesPatientAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
                when(currentUserService.isAdmin()).thenReturn(true);
                when(appointmentRepository.findByPatientId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByPatient(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByPatientId(1L, pageable);
            }

            @Test
            @DisplayName("Should return appointments when staff (receptionist) accesses patient appointments")
            void shouldReturnAppointmentsWhenReceptionistAccessesPatientAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(true);
                when(appointmentRepository.findByPatientId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByPatient(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByPatientId(1L, pageable);
            }
        }

        @Nested
        @DisplayName("Get Appointments By Doctor")
        class GetByDoctorAppointment {

            @Test
            @DisplayName("Should throw ResourceNotFound when doctor does not exist")
            void shouldThrowResourceNotFoundWhenDoctorDoesNotExist() {
                Pageable pageable = PageRequest.of(0, 10);

                when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

                ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class, () -> appointmentService.getByDoctor(1L, pageable));

                assertEquals("Doctor not found", resourceNotFound.getMessage());
                verify(appointmentRepository, never()).findByDoctorId(any(), any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when user is neither staff nor doctor")
            void shouldThrowAccessDeniedWhenUserIsNeitherStaffNorDoctor() {
                Pageable pageable = PageRequest.of(0, 10);

                Register doctorUser = new Register();
                doctorUser.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);

                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isDoctor()).thenReturn(false);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.getByDoctor(1L, pageable));

                assertEquals("You are not allowed to access doctor appointments", exception.getMessage());
                verify(appointmentRepository, never()).findByDoctorId(any(), any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when doctor tries to access another doctor's appointments")
            void shouldThrowAccessDeniedWhenDoctorAccessesAnotherDoctorsAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register doctorUser = new Register();
                doctorUser.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);

                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isDoctor()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(2L);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class,
                        () -> appointmentService.getByDoctor(1L, pageable));

                assertEquals("You are not allowed to access another doctor appointments", exception.getMessage());
                verify(appointmentRepository, never()).findByDoctorId(any(), any());
            }

            @Test
            @DisplayName("Should return appointments when doctor accesses their own appointments")
            void shouldReturnAppointmentsWhenDoctorAccessesOwnAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register doctorUser = new Register();
                doctorUser.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isDoctor()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(1L);
                when(appointmentRepository.findByDoctorId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByDoctor(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByDoctorId(1L, pageable);
            }

            @Test
            @DisplayName("Should return appointments when staff (admin) accesses doctor appointments")
            void shouldReturnAppointmentsWhenAdminAccessesDoctorAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register doctorUser = new Register();
                doctorUser.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(currentUserService.isAdmin()).thenReturn(true);
                when(appointmentRepository.findByDoctorId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByDoctor(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByDoctorId(1L, pageable);
            }

            @Test
            @DisplayName("Should return appointments when staff (receptionist) accesses doctor appointments")
            void shouldReturnAppointmentsWhenReceptionistAccessesDoctorAppointments() {
                Pageable pageable = PageRequest.of(0, 10);

                Register doctorUser = new Register();
                doctorUser.setId(1L);

                Doctor doctor = new Doctor();
                doctor.setUser(doctorUser);

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(true);
                when(appointmentRepository.findByDoctorId(1L, pageable)).thenReturn(appointmentPage);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                Page<AppointmentResponseDto> result = appointmentService.getByDoctor(1L, pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(responseDto, result.getContent().get(0));
                verify(appointmentRepository, times(1)).findByDoctorId(1L, pageable);
            }
        }

        @Nested
        @DisplayName("Cancel Appointment")
        class CancelAppointment {

            @Test
            @DisplayName("Should throw ResourceNotFound when appointment does not exist")
            void shouldThrowResourceNotFoundWhenAppointmentDoesNotExist() {
                when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

                ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class, () -> appointmentService.cancel(1L));

                assertEquals("Appointment not found", resourceNotFound.getMessage());
                verify(appointmentRepository, never()).save(any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when user is neither staff nor patient")
            void shouldThrowAccessDeniedWhenUserIsNeitherStaffNorPatient() {
                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(false);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.cancel(1L));

                assertEquals("You are not allowed to cancel this appointment", exception.getMessage());
                verify(appointmentRepository, never()).save(any());
            }

            @Test
            @DisplayName("Should throw AccessDeniedExceptionHandler when patient tries to cancel another patient's appointment")
            void shouldThrowAccessDeniedWhenPatientCancelsAnotherPatientsAppointment() {
                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                appointment.setPatient(patient);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(2L);

                AccessDeniedExceptionHandler exception = assertThrows(AccessDeniedExceptionHandler.class, () -> appointmentService.cancel(1L));

                assertEquals("You are not allowed to cancel another patient's appointment", exception.getMessage());
                verify(appointmentRepository, never()).save(any());
            }

            @Test
            @DisplayName("Should throw InvalidOperationException when patient cancels a non-pending appointment")
            void shouldThrowInvalidOperationExceptionWhenPatientCancelsNonPendingAppointment() {
                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                appointment.setPatient(patient);
                appointment.setStatus(AppointmentStatus.CONFIRMED);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(1L);

                InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> appointmentService.cancel(1L));

                assertEquals("You can only cancel an appointment while it is still pending", exception.getMessage());
                verify(appointmentRepository, never()).save(any());
            }

            @Test
            @DisplayName("Should cancel appointment successfully when patient cancels their own pending appointment")
            void shouldCancelAppointmentSuccessfullyWhenPatientCancelsOwnPendingAppointment() {
                Register patientUser = new Register();
                patientUser.setId(1L);

                Patient patient = new Patient();
                patient.setUser(patientUser);

                appointment.setPatient(patient);
                appointment.setStatus(AppointmentStatus.PENDING);

                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(false);
                when(currentUserService.isPatient()).thenReturn(true);
                when(currentUserService.getCurrentUserId()).thenReturn(1L);
                when(appointmentRepository.save(appointment)).thenReturn(appointment);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                AppointmentResponseDto result = appointmentService.cancel(1L);

                assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
                assertEquals(responseDto, result);
                verify(appointmentRepository, times(1)).save(appointment);
            }

            @Test
            @DisplayName("Should throw InvalidOperationException when staff cancels a completed appointment")
            void shouldThrowInvalidOperationExceptionWhenStaffCancelsCompletedAppointment() {
                appointment.setStatus(AppointmentStatus.COMPLETED);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(true);

                InvalidOperationException exception = assertThrows(InvalidOperationException.class, () -> appointmentService.cancel(1L));

                assertEquals("Cannot cancel a completed appointment", exception.getMessage());
                verify(appointmentRepository, never()).save(any());
            }

            @Test
            @DisplayName("Should cancel appointment successfully when staff (admin) cancels a non-completed appointment")
            void shouldCancelAppointmentSuccessfullyWhenAdminCancels() {
                appointment.setStatus(AppointmentStatus.CONFIRMED);

                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(true);
                when(appointmentRepository.save(appointment)).thenReturn(appointment);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                AppointmentResponseDto result = appointmentService.cancel(1L);

                assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
                assertEquals(responseDto, result);
                verify(appointmentRepository, times(1)).save(appointment);
            }

            @Test
            @DisplayName("Should cancel appointment successfully when staff (receptionist) cancels a pending appointment")
            void shouldCancelAppointmentSuccessfullyWhenReceptionistCancels() {
                appointment.setStatus(AppointmentStatus.PENDING);

                AppointmentResponseDto responseDto = mock(AppointmentResponseDto.class);

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
                when(currentUserService.isAdmin()).thenReturn(false);
                when(currentUserService.isReceptionist()).thenReturn(true);
                when(appointmentRepository.save(appointment)).thenReturn(appointment);
                when(appointmentMapper.toResponseDto(appointment)).thenReturn(responseDto);

                AppointmentResponseDto result = appointmentService.cancel(1L);

                assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
                assertEquals(responseDto, result);
                verify(appointmentRepository, times(1)).save(appointment);
            }
        }
    }
}}
