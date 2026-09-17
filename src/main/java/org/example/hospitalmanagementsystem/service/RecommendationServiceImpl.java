package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.RecommendedDoctorDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorRecommendationResponseDto;
import org.example.hospitalmanagementsystem.entity.Appointment;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.repository.AppointmentRepository;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final MedicalSpecializationService medicalSpecializationService;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;


    @Transactional(readOnly = true)
    @Override
    public DoctorRecommendationResponseDto recommendSpecialization(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Appointment with id" + id + "Not Found"));

        String problem = appointment.getReason();

        if (problem == null || problem.isBlank()) {
            throw new ResourceNotFound("Appointment does not contain a problem description");
        }

        Doctor bookedDoctor = appointment.getDoctor();

        String recommendedSpecialization = medicalSpecializationService.determineSpecialization(problem);


        if (!departmentRepository.existsByNameIgnoreCase(recommendedSpecialization)) {
            return DoctorRecommendationResponseDto.builder()
                    .appointmentId(appointment.getId())
                    .problem(problem)
                    .bookedDoctorName(getDoctorName(bookedDoctor))
                    .bookedDoctorSpecialization(bookedDoctor.getSpecialization())
                    .recommendedSpecialization(recommendedSpecialization)
                    .specializationMatches(false)
                    .recommendedDoctor(Collections.emptyList())
                    .message(String.format(
                            "Based on your problem, a %s specialist may be appropriate, but our hospital currently does not have a %s department.",
                            recommendedSpecialization, recommendedSpecialization
                    ))
                    .build();
        }

        boolean specializationMatches = bookedDoctor.getSpecialization().equalsIgnoreCase(recommendedSpecialization);

        List<RecommendedDoctorDto> recommendedDoctors = doctorRepository.findBySpecializationIgnoreCaseAndActiveTrue(
                                recommendedSpecialization
                        )
                        .stream()
                        .map(this::toDto)
                        .toList();

        String message = buildMessage(
                bookedDoctor,
                recommendedSpecialization,
                specializationMatches,
                recommendedDoctors
        );

        return DoctorRecommendationResponseDto.builder()
                .appointmentId(appointment.getId())
                .problem(problem)
                .bookedDoctorName(getDoctorName(bookedDoctor))
                .bookedDoctorSpecialization(bookedDoctor.getSpecialization())
                .recommendedSpecialization(recommendedSpecialization)
                .specializationMatches(specializationMatches)
                .message(message)
                .recommendedDoctor(recommendedDoctors)
                .build();
    }
    private RecommendedDoctorDto toDto(Doctor doctor) {

        return new RecommendedDoctorDto(
                doctor.getId(),
                getDoctorName(doctor),
                doctor.getSpecialization(),
                doctor.getDepartment().getName()
        );
    }

    private String getDoctorName(Doctor doctor) {

        return doctor.getUser().getFirstName()
                + " "
                + doctor.getUser().getLastName();
    }

    private String buildMessage(
            Doctor bookedDoctor,
            String recommendedSpecialization,
            boolean specializationMatches,
            List<RecommendedDoctorDto> doctors
    ) {

        if (specializationMatches) {

            return "Your current appointment is with a "
                    + recommendedSpecialization
                    + " specialist, which matches the problem "
                    + "you described.";
        }

        if (doctors.isEmpty()) {

            return "Based on the problem you described, a "
                    + recommendedSpecialization
                    + " specialist may be more appropriate. "
                    + "However, no active doctor with this "
                    + "specialization is currently available.";
        }

        return "Your current appointment is with a "
                + bookedDoctor.getSpecialization()
                + " specialist. Based on the problem you "
                + "described, a "
                + recommendedSpecialization
                + " specialist may be more appropriate.";
    }

}