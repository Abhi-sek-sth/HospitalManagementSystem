package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.DoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.request.UpdateDoctorRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DoctorResponseDto;
import org.example.hospitalmanagementsystem.entity.Department;
import org.example.hospitalmanagementsystem.entity.Doctor;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.InvalidOperationException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.DoctorMapper;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.example.hospitalmanagementsystem.repository.DoctorRepository;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService{
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final RegisterRepository registerRepository;
    private final DepartmentRepository departmentRepository;
    @Override
    public DoctorResponseDto create(DoctorRequestDto requestDto) {
        if(doctorRepository.existsByLicenseNumber(requestDto.licenseNumber())){
            throw new AlreadyExistException("License Number already exists");
        }

        if(doctorRepository.existsByUserId(requestDto.userId())){
            throw new AlreadyExistException("User id already register as a doctor");
        }

        Register user = registerRepository.findById(requestDto.userId()).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Department department = departmentRepository.findById(requestDto.departmentId()).orElseThrow(() -> new ResourceNotFoundException("Department Not Found"));

        Doctor doctor = doctorMapper.toEntity(requestDto);
        doctor.setUser(user);
        doctor.setDepartment(department);

        Doctor saveDoctor = doctorRepository.save(doctor);

        return doctorMapper.toResponseDto(saveDoctor);
    }

    @Override
    public DoctorResponseDto getById(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor Not Found"));
        return doctorMapper.toResponseDto(doctor);
    }

    @Override
    public DoctorResponseDto update(Long id, UpdateDoctorRequestDto requestDto) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor Not Found"));

        Department department = departmentRepository.findById(requestDto.departmentId()).orElseThrow(() -> new ResourceNotFoundException("Department Not Found"));

        doctor.setActive(requestDto.active());
        doctor.setSpecialization(requestDto.specialization());
        doctor.setPhone(requestDto.phone());
        doctor.setDepartment(department);

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDto(updatedDoctor);
    }

    @Override
    public void delete(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor Not Found"));
        doctorRepository.delete(doctor);

    }

    @Override
    public DoctorPageResponseDto<DoctorResponseDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Doctor> doctors = doctorRepository.findAll(pageable);

        return new DoctorPageResponseDto<>(
                doctors.getContent()
                        .stream()
                        .map(doctorMapper::toResponseDto)
                        .toList(),
                doctors.getNumber(),
                doctors.getTotalElements(),
                doctors.getTotalPages()

        );

    }

    @Override
    @Transactional
    public void deactivate(Long id) {

        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (!doctor.getActive()) {
            throw new InvalidOperationException("Doctor is already inactive");
        }

        doctor.setActive(false);
    }

    @Override
    @Transactional
    public void activate(Long id) {

        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (doctor.getActive()) {
            throw new InvalidOperationException("Doctor is already active");
        }

        doctor.setActive(true);
    }
}
