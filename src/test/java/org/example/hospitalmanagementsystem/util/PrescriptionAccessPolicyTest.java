package org.example.hospitalmanagementsystem.util;

import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.AppointmentStatus;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.Patient;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.AppointmentNotCompletedException;
import org.example.hospitalmanagementsystem.exception.ForbiddenOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionAccessPolicyTest {

    private static final Long DOCTOR_USER_ID = 1L;
    private static final Long PATIENT_USER_ID = 2L;
    private static final Long STRANGER_USER_ID = 3L;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private PrescriptionAccessPolicy policy;

    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();                       // active defaults to TRUE via field initializer
        doctor.setUser(userWithId(DOCTOR_USER_ID));

        patient = Patient.builder().user(userWithId(PATIENT_USER_ID)).build();

        appointment = Appointment.builder()
                .id(50L)
                .doctor(doctor)
                .patient(patient)
                .status(AppointmentStatus.COMPLETED)
                .build();
    }

    @Nested
    @DisplayName("requireDoctorRole")
    class RequireDoctorRole {

        @Test
        void requireDoctorRole_shouldNotThrow_whenUserIsDoctor() {
            when(securityUtil.isDoctor()).thenReturn(true);

            assertThatCode(() -> policy.requireDoctorRole("create")).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {"create", "modify", "delete"})
        void requireDoctorRole_shouldThrowForbiddenOperationException_whenUserIsNotDoctor(String operation) {
            when(securityUtil.isDoctor()).thenReturn(false);

            assertThatThrownBy(() -> policy.requireDoctorRole(operation))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("Only doctors can " + operation + " prescriptions");
        }
    }

    @Nested
    @DisplayName("requireOwningDoctor")
    class RequireOwningDoctor {

        @Test
        void requireOwningDoctor_shouldNotThrowAndNotEvaluateMessage_whenUserIsOwningDoctor() {
            AtomicInteger supplierCalls = new AtomicInteger();

            assertThatCode(() -> policy.requireOwningDoctor(appointment, DOCTOR_USER_ID, () -> {
                supplierCalls.incrementAndGet();
                return "unused";
            })).doesNotThrowAnyException();

            assertThat(supplierCalls).hasValue(0);
            verifyNoInteractions(securityUtil);
        }

        @Test
        void requireOwningDoctor_shouldThrowForbiddenOperationExceptionWithSuppliedMessage_whenUserIsNotOwningDoctor() {
            assertThatThrownBy(() -> policy.requireOwningDoctor(
                    appointment, STRANGER_USER_ID, () -> "custom forbidden message"))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("custom forbidden message");
        }

        @Test
        void requireOwningDoctor_shouldThrowForbiddenOperationException_whenUserIsThePatientOfTheAppointment() {
            assertThatThrownBy(() -> policy.requireOwningDoctor(
                    appointment, PATIENT_USER_ID, () -> "patients cannot act as doctor"))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("patients cannot act as doctor");
        }
    }

    @Nested
    @DisplayName("requireParticipant")
    class RequireParticipant {

        @Test
        void requireParticipant_shouldNotThrow_whenUserIsOwningDoctor() {
            assertThatCode(() -> policy.requireParticipant(appointment, DOCTOR_USER_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        void requireParticipant_shouldNotThrow_whenUserIsOwningPatient() {
            assertThatCode(() -> policy.requireParticipant(appointment, PATIENT_USER_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        void requireParticipant_shouldThrowForbiddenOperationException_whenUserIsNeitherDoctorNorPatient() {
            assertThatThrownBy(() -> policy.requireParticipant(appointment, STRANGER_USER_ID))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("You are not allowed to access this appointment");
        }
    }

    @Nested
    @DisplayName("requireCompleted")
    class RequireCompleted {

        @Test
        void requireCompleted_shouldNotThrow_whenAppointmentIsCompleted() {
            assertThatCode(() -> policy.requireCompleted(appointment)).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @EnumSource(value = AppointmentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "COMPLETED")
        void requireCompleted_shouldThrowAppointmentNotCompletedException_whenStatusIsNotCompleted(
                AppointmentStatus status) {
            appointment.setStatus(status);

            assertThatThrownBy(() -> policy.requireCompleted(appointment))
                    .isInstanceOf(AppointmentNotCompletedException.class)
                    .hasMessage("Your appointment has not been completed yet");
        }

        @Test
        void requireCompleted_shouldThrowAppointmentNotCompletedException_whenAppointmentIsNull() {
            assertThatThrownBy(() -> policy.requireCompleted(null))
                    .isInstanceOf(AppointmentNotCompletedException.class)
                    .hasMessage("Your appointment has not been completed yet");
        }
    }

    @Nested
    @DisplayName("requireActiveDoctor")
    class RequireActiveDoctor {

        @Test
        void requireActiveDoctor_shouldNotThrow_whenDoctorIsActive() {
            doctor.setActive(true);

            assertThatCode(() -> policy.requireActiveDoctor(doctor, "inactive")).doesNotThrowAnyException();
        }

        @Test
        void requireActiveDoctor_shouldThrowForbiddenOperationException_whenDoctorIsInactive() {
            doctor.setActive(false);

            assertThatThrownBy(() -> policy.requireActiveDoctor(doctor, "Doctor is inactive"))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("Doctor is inactive");
        }

        @Test
        void requireActiveDoctor_shouldThrowForbiddenOperationException_whenActiveFlagIsNull() {
            doctor.setActive(null);

            assertThatThrownBy(() -> policy.requireActiveDoctor(doctor, "Doctor is inactive"))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("Doctor is inactive");
        }
    }

    private static Register userWithId(Long id) {
        Register user = new Register();
        user.setId(id);
        return user;
    }
}