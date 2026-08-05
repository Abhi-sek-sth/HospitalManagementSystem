package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.DepartmentRequestDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentPageResponseDto;
import org.example.hospitalmanagementsystem.dto.response.DepartmentResponseDto;
import org.example.hospitalmanagementsystem.entity.Department;
import org.example.hospitalmanagementsystem.exception.AlreadyExistException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFound;
import org.example.hospitalmanagementsystem.mapper.DepartmentMapper;
import org.example.hospitalmanagementsystem.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentServiceImpl Unit Tests")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequestDto requestDto;
    private Department department;
    private Department department2;
    private DepartmentResponseDto responseDto;
    private DepartmentResponseDto responseDto2;
    private DepartmentRequestDto updateRequestDto;
    private DepartmentResponseDto updateResponseDto;


    @BeforeEach
    void setUp() {

        requestDto = DepartmentRequestDto.builder()
                .name("Cardiology")
                .location("4th Floor")
                .description("Heart Department")
                .build();

        updateRequestDto = DepartmentRequestDto.builder()
                .name("Neurology")
                .location("5th floor")
                .description("Brain Related")
                .build();

        department = Department.builder()
                .id(1L)
                .name("Cardiology")
                .location("4th Floor")
                .description("Heart Department")
                .createdAt(LocalDateTime.now())
                .build();

        department2=Department.builder()
                .id(2L)
                .name("Neurology")
                .location("5th Floor")
                .description("Brain Department")
                .createdAt(LocalDateTime.now())
                .build();

        responseDto = DepartmentResponseDto.builder()
                .id(1L)
                .name("Cardiology")
                .location("4th Floor")
                .description("Heart Department")
                .createdAt(department.getCreatedAt())
                .build();


        responseDto2 = DepartmentResponseDto.builder()
                .id(1L)
                .name("Neurology")
                .location("5th Floor")
                .description("Brain Department")
                .createdAt(department.getCreatedAt())
                .build();


        updateResponseDto = DepartmentResponseDto.builder()
                .id(1L)
                .name("Neurology")
                .location("5th Floor")
                .description("Brain Related")
                .createdAt(department.getCreatedAt())
                .build();



    }

    @Nested
    @DisplayName("Create Department")
    class CreateDepartment {

        @Test
        @DisplayName("Should create department successfully when department does not exist")
        void shouldCreateDepartmentSuccessfully() {

            // Arrange
            when(departmentRepository.existsByName(requestDto.name()))
                    .thenReturn(false);

            when(departmentMapper.toEntity(requestDto))
                    .thenReturn(department);

            when(departmentRepository.save(department))
                    .thenReturn(department);

            when(departmentMapper.toResponse(department))
                    .thenReturn(responseDto);

            // Act
            DepartmentResponseDto result = departmentService.create(requestDto);

            // Assert
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(1L, result.id()),
                    () -> assertEquals("Cardiology", result.name()),
                    () -> assertEquals("4th Floor", result.location()),
                    () -> assertEquals("Heart Department", result.description())
            );

            verify(departmentRepository).existsByName(requestDto.name());
            verify(departmentMapper).toEntity(requestDto);
            verify(departmentRepository).save(department);
            verify(departmentMapper).toResponse(department);

            verifyNoMoreInteractions(
                    departmentRepository,
                    departmentMapper
            );
        }
        @Test
        @DisplayName("should throw exception when department already exists")
         void shouldThrowAlreadyExistExceptionWhenDepartmentNameAlreadyExists(){
            when(departmentRepository.existsByName(requestDto.name())).thenReturn(true);
            final AlreadyExistException exception = assertThrows(AlreadyExistException.class,() -> departmentService.create(requestDto));

            assertEquals("Department already exists",exception.getMessage());


            verify(departmentRepository).existsByName(requestDto.name());
            //exits service after detecting a duplicate
            verify(departmentRepository, never()).save(any());
        }}
        @Nested
        @DisplayName("update department")
        class updateDepartment {

            @Test
            @DisplayName("should update department if department already exists")
            void shouldUpdateDepartmentIfDepartmentExists() {


                when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
                when(departmentRepository.existsByName(updateRequestDto.name())).thenReturn(false);
                when(departmentRepository.save(department)).thenReturn(department);
                when(departmentMapper.toResponse(department)).thenReturn(updateResponseDto);

                DepartmentResponseDto result = departmentService.update(1L, updateRequestDto);

                assertEquals(updateResponseDto, result);

                assertEquals(updateRequestDto.name(), department.getName());
                assertEquals(updateRequestDto.description(), department.getDescription());
                assertEquals(updateRequestDto.location(), department.getLocation());

                verify(departmentRepository).findById(1L);
                verify(departmentRepository).existsByName(updateRequestDto.name());
                verify(departmentRepository).save(department);
                verify(departmentMapper).toResponse(department);

                verifyNoMoreInteractions(departmentRepository, departmentMapper);

            }

            @Test
            @DisplayName("Should throw Resource not found exception if id not found")
            void shouldThrowResourceNotFoundException() {
                when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

                ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> departmentService.update(1L, updateRequestDto));

                assertEquals("Department not found", exception.getMessage());
                verify(departmentRepository).findById(1L);
                verify(departmentRepository, never()).save(any());
                verifyNoMoreInteractions(departmentMapper);
            }
            @Test
            @DisplayName("should throw already exist exception")
            void shouldThrowAlreadyExistException(){
                when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
                when(departmentRepository.existsByName(updateRequestDto.name())).thenReturn(true);
                final AlreadyExistException exception = assertThrows(AlreadyExistException.class,() -> departmentService.update(1L,updateRequestDto));

                assertEquals("Department already exists",exception.getMessage());

                verify(departmentRepository).findById(1L);
                verify(departmentRepository).existsByName(updateRequestDto.name());
                //exits service after detecting a duplicate
                verify(departmentRepository, never()).save(any());
                verify(departmentMapper,never()).toResponse(any());

                verifyNoMoreInteractions(departmentRepository,departmentMapper);
            }


        }
        @Nested
        @DisplayName("should display all department")
        class getAllDepartment{
            @Test
            void shouldGetAllDepartment(){
                Page<Department> departmentPageList = new PageImpl<>(List.of(department,department2),PageRequest.of(0,5),2);
                when(departmentRepository.findAll(any(Pageable.class)))
                        .thenReturn(departmentPageList);

                when(departmentMapper.toResponse(department))
                        .thenReturn(responseDto);

                when(departmentMapper.toResponse(department2))
                        .thenReturn(responseDto2);

                DepartmentPageResponseDto<DepartmentResponseDto> result =
                        departmentService.getAll(0,5);

                assertEquals(2, result.totalElements());
                assertEquals(1, result.totalPages());
                assertEquals(0, result.currentPage());

                assertEquals(2, result.content().size());
                assertEquals(responseDto, result.content().get(0));
                assertEquals(responseDto2, result.content().get(1));

                verify(departmentRepository).findAll(any(Pageable.class));
                verify(departmentMapper).toResponse(department);
                verify(departmentMapper).toResponse(department2);

                verifyNoMoreInteractions(
                        departmentRepository,
                        departmentMapper
                );






            }}
            @Nested
            class getDepartmentById{
                @Test
                @DisplayName("should get department by id")
                void shouldGetDepartmentById(){
                    when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
                    when(departmentMapper.toResponse(department)).thenReturn(responseDto);

                    DepartmentResponseDto result = departmentService.getById(1L);

                    assertEquals(responseDto,result);

                    verify(departmentRepository).findById(1L);
                    verify(departmentMapper).toResponse(department);

                }
                @Test
                @DisplayName("Should throw Resource not found exception if department id not found")
                void shouldThrowResourceNotFoundException() {
                    when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

                    ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> departmentService.getById(1L));

                    assertEquals("Department not found", exception.getMessage());
                    verify(departmentRepository).findById(1L);
                    verify(departmentRepository, never()).save(any());
                    verifyNoMoreInteractions(departmentMapper);
                }

                @Nested
                class deleteById{
                    @Test
                    @DisplayName("should delete department if found")
                    void shouldDeleteById(){
                        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

                           departmentService.delete(1L);

                           verify(departmentRepository).findById(1L);
                           verify(departmentRepository).delete(department);

                           verifyNoMoreInteractions(departmentRepository);
                    }
                    @Test
                    @DisplayName("Should throw Resource not found exception if department id not found")
                    void shouldThrowResourceNotFoundException() {
                        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

                        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> departmentService.delete(1L));

                        assertEquals("Department Not Found", exception.getMessage());
                        verify(departmentRepository).findById(1L);
                        verify(departmentRepository, never()).delete(any());
                        verifyNoMoreInteractions(departmentRepository);
                    }
                }



            }






}