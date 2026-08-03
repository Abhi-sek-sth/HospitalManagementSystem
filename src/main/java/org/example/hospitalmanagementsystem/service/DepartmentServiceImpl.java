package org.example.hospitalmanagementsystem.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.DepartmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentResponseDto;
import org.example.hospitalmanagementsystem.entity.Department;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.mapper.DepartmentMapper;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService{
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    @Override
    public DepartmentResponseDto create(DepartmentRequestDto requestDto) {
       if(departmentRepository.existsByName(requestDto.name())){
           throw new AlreadyExistException("Department already exists");
       }
       Department department = departmentMapper.toEntity(requestDto);
       Department saveDepartment = departmentRepository.save(department);
        return departmentMapper.toResponse(saveDepartment);
    }

    @Override
    public DepartmentResponseDto update(Long id, DepartmentRequestDto requestDto) {
        Department existing = departmentRepository.findById(id).orElseThrow(() ->new ResourceNotFound("Department not found"));
        if (!existing.getName().equals(requestDto.name())
                && departmentRepository.existsByName(requestDto.name())) {

            throw new AlreadyExistException("Department already exists");
        }


existing.setDescription(requestDto.description());
existing.setName(requestDto.name());
existing.setLocation(requestDto.location());
Department saveDepartment = departmentRepository.save(existing);
        return departmentMapper.toResponse(saveDepartment);
    }

    @Override
    public DepartmentResponseDto getById(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Department not found"));
        return departmentMapper.toResponse(department);
    }

    @Override
    public DepartmentPageResponseDto<DepartmentResponseDto> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Department> departments = departmentRepository.findAll(pageable);

        return new DepartmentPageResponseDto<>(
                departments.getContent()
                        .stream()
                        .map(departmentMapper::toResponse)
                        .toList(),
                departments.getNumber(),
                departments.getTotalElements(),
                departments.getTotalPages()

        );
    }

    @Override
    public void delete(Long id) {
        Department department = departmentRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Department Not Found"));
departmentRepository.delete(department);

    }
}
