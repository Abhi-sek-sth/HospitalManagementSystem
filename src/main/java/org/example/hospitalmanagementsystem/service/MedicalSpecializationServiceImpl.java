package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalSpecializationServiceImpl
        implements MedicalSpecializationService {

    private final ChatClient groqChatClient;

    @Override
    public String determineSpecialization(String problem) {

        String result = groqChatClient.prompt()
                .system("""
                You are a medical department classification assistant
                for a hospital appointment system.

                Your ONLY task is to identify the most appropriate
                hospital department based on the patient's problem.

                IMPORTANT:
                - Return the hospital department name, NOT the doctor's
                  professional title.
                - For example, return "Cardiology", NOT "Cardiologist".
                - Return "Neurology", NOT "Neurologist".
                - Return "Dermatology", NOT "Dermatologist".

                Rules:
                - Return ONLY the department name.
                - Do NOT diagnose the patient.
                - Do NOT provide treatment.
                - Do NOT prescribe medicine.
                - Do NOT recommend medication.
                - Do NOT provide emergency instructions.
                - Do NOT explain your answer.
                - Do NOT include punctuation or additional text.
                - Use a standard hospital department name.

                Examples:

                Headache -> Neurology
                Skin rash -> Dermatology
                Tooth pain -> Dentistry
                Eye pain -> Ophthalmology
                Heart problem -> Cardiology
                Bone or joint pain -> Orthopedics
                Stomach pain -> Gastroenterology
                Ear pain -> ENT
                Pregnancy related problem -> Gynecology
                Child health problem -> Pediatrics

                The output must represent a hospital department,
                not a doctor's title or profession.
                """)
                .user(problem)
                .call()
                .content();



        if (result == null || result.isBlank()) {
            throw new IllegalStateException(
                    "AI could not determine a medical specialization"
            );
        }

        return result.trim();
    }
}