package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.PatientRequestDto;
import org.example.hospitalmanagementsystem.dto.request.PatientUpdateRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PatientPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PatientResponseDto;
import org.example.hospitalmanagementsystem.entity.Patient;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.mapper.PatientMapper;
import org.example.hospitalmanagementsystem.repository.PatientRepository;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final RegisterRepository registerRepository;

    @Override
    public PatientResponseDto create(PatientRequestDto requestDto) {

        if(patientRepository.existsByUserId(requestDto.userId())){
            throw new AlreadyExistException("Patient already exists for this user");
        }

        if (patientRepository.existsByPhone(requestDto.phone())) {
            throw new AlreadyExistException(
                    "Patient with this phone number already exists"
            );
        }

        Register user = registerRepository.findById(requestDto.userId())
                .orElseThrow(() ->
                        new ResourceNotFound("User Not Found")
                );

        Patient patient = patientMapper.toEntity(requestDto);

        patient.setUser(user);

        Patient savedPatient = patientRepository.save(patient);

        return patientMapper.toResponseDto(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFound("Patient Not Found")
                );

        return patientMapper.toResponseDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientPageResponseDto<PatientResponseDto> getAll(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Patient> patientPage =
                patientRepository.findAll(pageable);

        return new PatientPageResponseDto<>(
                patientPage.getContent()
                        .stream()
                        .map(patientMapper::toResponseDto)
                        .toList(),
                patientPage.getNumber(),
                patientPage.getTotalElements(),
                patientPage.getTotalPages()
        );
    }

    @Override
    public PatientResponseDto update(Long id, PatientUpdateRequestDto requestDto) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFound("Patient Not Found")
                );

        if (patientRepository.existsByPhoneAndIdNot(
                requestDto.phone(),
                id
        )) {
            throw new AlreadyExistException(
                    "Patient with this phone number already exists"
            );
        }

        patientMapper.updateEntity(requestDto, patient);


        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toResponseDto(updatedPatient);
    }

    @Override
    public void delete(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFound("Patient Not Found")
                );

        patientRepository.delete(patient);
    }
}