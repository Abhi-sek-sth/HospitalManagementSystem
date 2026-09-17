package org.example.hospitalmanagementsystem.service;


import org.example.hospitalmanagementsystem.dto.request.RecommendedDoctorDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorRecommendationResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.Department;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {
    @Mock
    private MedicalSpecializationService medicalSpecializationService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private static final Long APPOINTMENT_ID = 1L;
    private static final String PROBLEM_DESCRIPTION = "Severe chest pain and shortness of breath";
    private static final String RECOMMENDED_SPECIALIZATION = "Cardiology";

    private Appointment appointment;
    private Doctor bookedDoctor;
    private Register bookedDoctorUser;
    private DoctorRecommendationResponseDto doctorRecommendationResponseDto;
    private Department department;

    @BeforeEach
    void setUp() {

        bookedDoctorUser = new Register();
        bookedDoctorUser.setFirstName("John");
        bookedDoctorUser.setLastName("Smith");

        Department department = new Department();
        department.setName("Cardiology");

        bookedDoctor = new Doctor();
        bookedDoctor.setId(10L);
        bookedDoctor.setUser(bookedDoctorUser);
        bookedDoctor.setSpecialization("General Medicine");
        bookedDoctor.setDepartment(department);

        appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setReason(PROBLEM_DESCRIPTION);
        appointment.setDoctor(bookedDoctor);
    }

    @Nested
    @DisplayName("Appointment lookup validation")
    class AppointmentLookupTests {

        @Test
        @DisplayName("throws ResourceNotFound when appointment does not exist")
        void shouldThrowWhenAppointmentNotFound() {
            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

            ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> recommendationService.recommendSpecialization(APPOINTMENT_ID));

            assertEquals("Appointment with id" + APPOINTMENT_ID + "Not Found",exception.getMessage());
            verify(medicalSpecializationService, never()).determineSpecialization(anyString());
            verify(departmentRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFound when appointment reason is null")
        void shouldThrowWhenReasonIsNull() {
            appointment.setReason(null);
            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
            ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> recommendationService.recommendSpecialization(APPOINTMENT_ID));
            assertEquals("Appointment does not contain a problem description",exception.getMessage());


            verify(medicalSpecializationService, never()).determineSpecialization(anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFound when appointment reason is blank")
        void shouldThrowWhenReasonIsBlank() {
            appointment.setReason("   ");
            when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

            ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> recommendationService.recommendSpecialization(APPOINTMENT_ID));

            assertEquals("Appointment does not contain a problem description",exception.getMessage());

            verify(medicalSpecializationService, never()).determineSpecialization(anyString());
        }
    }

    // ------------------------------------------------------------------
    // Department does not exist branch
    // ------------------------------------------------------------------

    @Test
    @DisplayName("returns early with no recommended doctors when recommended department does not exist")
    void shouldReturnEarlyWhenDepartmentDoesNotExist() {
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(medicalSpecializationService.determineSpecialization(PROBLEM_DESCRIPTION)).thenReturn(RECOMMENDED_SPECIALIZATION);
        when(departmentRepository.existsByNameIgnoreCase(RECOMMENDED_SPECIALIZATION)).thenReturn(false);

        DoctorRecommendationResponseDto result = recommendationService.recommendSpecialization(APPOINTMENT_ID);

        assertThat(result.appointmentId()).isEqualTo(APPOINTMENT_ID);
        assertThat(result.problem()).isEqualTo(PROBLEM_DESCRIPTION);
        assertThat(result.bookedDoctorName()).isEqualTo("John Smith");
        assertThat(result.bookedDoctorSpecialization()).isEqualTo("General Medicine");
        assertThat(result.recommendedSpecialization()).isEqualTo(RECOMMENDED_SPECIALIZATION);
        assertThat(result.specializationMatches()).isFalse();
        assertThat(result.recommendedDoctor()).isEmpty();
        assertThat(result.message()).contains(RECOMMENDED_SPECIALIZATION).contains("does not have a");


        verify(doctorRepository, never()).findBySpecializationIgnoreCaseAndActiveTrue(anyString());
    }

    @Test
    @DisplayName("returns match message when booked doctor's specialization matches recommendation")
    void shouldReturnMatchMessageWhenSpecializationsMatch() {
        bookedDoctor.setSpecialization("Cardiology");

        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(medicalSpecializationService.determineSpecialization(PROBLEM_DESCRIPTION)).thenReturn(RECOMMENDED_SPECIALIZATION);
        when(departmentRepository.existsByNameIgnoreCase(RECOMMENDED_SPECIALIZATION)).thenReturn(true);
        when(doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(RECOMMENDED_SPECIALIZATION)).thenReturn(List.of(bookedDoctor));

        DoctorRecommendationResponseDto result = recommendationService.recommendSpecialization(APPOINTMENT_ID);

        assertThat(result.specializationMatches()).isTrue();
        assertThat(result.message()).contains("matches the problem").contains(RECOMMENDED_SPECIALIZATION);
        assertThat(result.recommendedDoctor()).hasSize(1);
    }

    @Test
    @DisplayName("returns 'no doctor available' message when specializations differ and none found")
    void shouldReturnNoDoctorsAvailableMessage() {
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(medicalSpecializationService.determineSpecialization(PROBLEM_DESCRIPTION)).thenReturn(RECOMMENDED_SPECIALIZATION);
        when(departmentRepository.existsByNameIgnoreCase(RECOMMENDED_SPECIALIZATION)).thenReturn(true);
        when(doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(RECOMMENDED_SPECIALIZATION)).thenReturn(List.of());

        DoctorRecommendationResponseDto result = recommendationService.recommendSpecialization(APPOINTMENT_ID);

        assertThat(result.specializationMatches()).isFalse();
        assertThat(result.recommendedDoctor()).isEmpty();
        assertThat(result.message()).contains("no active doctor").contains(RECOMMENDED_SPECIALIZATION);
    }

    @Test
    @DisplayName("returns recommended doctors mapped to DTOs when specializations differ")
    void shouldReturnRecommendedDoctorsWhenSpecializationsDiffer() {
        Register otherUser = new Register();
        otherUser.setFirstName("Jane");
        otherUser.setLastName("Doe");

        Department cardiologyDept = new Department();
        cardiologyDept.setName("Cardiology");

        Doctor otherDoctor = new Doctor();
        otherDoctor.setId(20L);
        otherDoctor.setUser(otherUser);
        otherDoctor.setSpecialization("Cardiology");
        otherDoctor.setDepartment(cardiologyDept);

        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(medicalSpecializationService.determineSpecialization(PROBLEM_DESCRIPTION))
                .thenReturn(RECOMMENDED_SPECIALIZATION);
        when(departmentRepository.existsByNameIgnoreCase(RECOMMENDED_SPECIALIZATION)).thenReturn(true);
        when(doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(RECOMMENDED_SPECIALIZATION))
                .thenReturn(List.of(otherDoctor));

        DoctorRecommendationResponseDto result =
                recommendationService.recommendSpecialization(APPOINTMENT_ID);

        assertThat(result.specializationMatches()).isFalse();
        assertThat(result.recommendedDoctor()).hasSize(1);

        RecommendedDoctorDto dto = result.recommendedDoctor().get(0);
        assertThat(dto.doctorId()).isEqualTo(20L);
        assertThat(dto.doctorName()).isEqualTo("Jane Doe");
        assertThat(dto.specialization()).isEqualTo("Cardiology");
        assertThat(dto.department()).isEqualTo("Cardiology");

        assertThat(result.message()).contains("General Medicine").contains(RECOMMENDED_SPECIALIZATION);

        verify(doctorRepository, times(1)).findBySpecializationIgnoreCaseAndActiveTrue(eq(RECOMMENDED_SPECIALIZATION));
    }
}