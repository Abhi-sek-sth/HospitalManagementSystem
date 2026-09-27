package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.RegisterRequestDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RegisterResponseDto;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.entity.Roles;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.RegisterMapper;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.example.hospitalmanagementsystem.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;



@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService{
    private final RegisterRepository registerRepository;
    private final RegisterMapper registerMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public RegisterResponseDto register(RegisterRequestDto requestDto) {
        if (registerRepository.findByUsername(requestDto.username()).isPresent()) {
            throw new AlreadyExistException("User already exist");
        }

        if (registerRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new AlreadyExistException("Email already exist");
        }
        Register register = registerMapper.toEntity(requestDto);

        register.setPassword(passwordEncoder.encode(requestDto.password()));
        register.setCreatedAt(LocalDateTime.now());

        Roles roles = roleRepository.findByRoleName(requestDto.role()).orElseThrow(() -> new ResourceNotFoundException("Role not Found" + requestDto.role()));

        register.setRole(roles.getRoleName());

        Register savedRegister = registerRepository.save(register);

        return registerMapper.toResponseDto(savedRegister);
    }

    @Override
    public PageResponseDto<RegisterResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Register> users = registerRepository.findAll(pageable);

        return new PageResponseDto<>(
                users.getContent()
                        .stream()
                        .map(registerMapper::toResponseDto)
                        .toList(),
        users.getNumber(),
                users.getTotalPages(),
                users.getTotalElements()
    );
    }

    @Override
    public void deleteUser(Long id) {
        Register register = registerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("A user with id" + id + "not found"));
        registerRepository.delete(register);
    }

    @Override
    public RegisterResponseDto getById(Long id) {
        Register register = registerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("A user not found"));
        return registerMapper.toResponseDto(register);

    }

    @Override
    public RegisterResponseDto update(RegisterRequestDto registerRequestDto, Long id) {
        Register register = registerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("A user not found"));

        if (!register.getEmail().equals(registerRequestDto.email())
                && registerRepository.existsByEmail(registerRequestDto.email())) {
            throw new AlreadyExistException("Email already exists.");
        }
        register.setRole(registerRequestDto.role());
register.setFirstName(registerRequestDto.firstName());
register.setLastName(registerRequestDto.lastName());
register.setEmail(registerRequestDto.email());

        return registerMapper.toResponseDto(registerRepository.save(register));
    }
}
