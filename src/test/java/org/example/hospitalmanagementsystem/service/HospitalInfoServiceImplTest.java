package org.example.hospitalmanagementsystem.service;

import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.dto.response.HospitalInfoResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.HospitalInfo;
import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.HospitalInfoMapper;
import org.example.hospitalmanagementsystem.repository.HospitalInfoRepository;
import org.example.hospitalmanagementsystem.util.HospitalInfoActivateEvent;
import org.example.hospitalmanagementsystem.util.HospitalInfoCreatedEvent;
import org.example.hospitalmanagementsystem.util.HospitalInfoDeactivatedEvent;
import org.example.hospitalmanagementsystem.util.HospitalInfoDeleteEvent;
import org.example.hospitalmanagementsystem.util.HospitalInfoUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalInfoServiceImplTest {

    @Mock
    private HospitalInfoMapper hospitalInfoMapper;

    @Mock
    private HospitalInfoRepository hospitalInfoRepository;

    @Mock
    private VectorIndexService vectorIndexService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private HospitalInfoServiceImpl hospitalInfoService;

    private HospitalInfoRequestDto requestDto;
    private HospitalInfo hospitalInfo;
    private HospitalInfoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new HospitalInfoRequestDto(InformationCategory.EMERGENCY, "Emergency Information", "Emergency department details");

        hospitalInfo = new HospitalInfo();
        hospitalInfo.setId(1L);
        hospitalInfo.setTitle("Emergency Information");
        hospitalInfo.setDescription("Emergency department details");
        hospitalInfo.setCategory(InformationCategory.EMERGENCY);
        hospitalInfo.setActive(true);

        responseDto = new HospitalInfoResponseDto(1L, InformationCategory.EMERGENCY, "Emergency Information", "Emergency department details", true, LocalDateTime.now(), LocalDateTime.now().minusMinutes(10));
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        void create_shouldSaveHospitalInfoPublishEventAndReturnResponse() {

            when(hospitalInfoMapper.toEntity(requestDto)).thenReturn(hospitalInfo);
            when(hospitalInfoRepository.save(hospitalInfo)).thenReturn(hospitalInfo);
            when(hospitalInfoMapper.toResponseDto(hospitalInfo)).thenReturn(responseDto);

            HospitalInfoResponseDto result = hospitalInfoService.create(requestDto);

            assertThat(result).isSameAs(responseDto);
            assertThat(hospitalInfo.getActive()).isTrue();

            verify(hospitalInfoMapper).toEntity(requestDto);
            verify(hospitalInfoRepository).save(hospitalInfo);
            verify(hospitalInfoMapper).toResponseDto(hospitalInfo);
            verify(eventPublisher).publishEvent(ArgumentMatchers.any(HospitalInfoCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {
        @Test
        void getById_shouldReturnHospitalInfo() {
            when(hospitalInfoRepository.findById(1L)).thenReturn(Optional.of(hospitalInfo));
            when(hospitalInfoMapper.toResponseDto(hospitalInfo)).thenReturn(responseDto);

            HospitalInfoResponseDto result = hospitalInfoService.getById(1L);

            assertThat(result).isSameAs(responseDto);

            verify(hospitalInfoRepository).findById(1L);
            verify(hospitalInfoMapper).toResponseDto(hospitalInfo);
        }

        @Test
        void getById_shouldThrowResourceNotFoundWhenHospitalInfoDoesNotExist() {
            when(hospitalInfoRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException resourceNotFound = assertThrows(ResourceNotFoundException.class, () -> hospitalInfoService.getById(99L));
            assertEquals("Hospital information not found with id: " + 99L, resourceNotFound.getMessage());

            verify(hospitalInfoMapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {
        @Test
        void getAll_shouldReturnPagedHospitalInformation() {
            Page<HospitalInfo> page = new PageImpl<>(List.of(hospitalInfo), Pageable.ofSize(10), 1);

            when(hospitalInfoRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);
            when(hospitalInfoMapper.toResponseDto(hospitalInfo)).thenReturn(responseDto);

            PageResponseDto<HospitalInfoResponseDto> result = hospitalInfoService.getAll(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.content()).containsExactly(responseDto);
            assertThat(result.currentPage()).isEqualTo(0);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.totalElements()).isEqualTo(1);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(hospitalInfoRepository).findByActiveTrue(pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }

        @Test
        void getAll_shouldLimitPageSizeTo100() {
            when(hospitalInfoRepository.findByActiveTrue(any(Pageable.class))).thenReturn(Page.empty());

            hospitalInfoService.getAll(0, 500);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(hospitalInfoRepository).findByActiveTrue(pageableCaptor.capture());

            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
        }

        @Test
        void getAll_shouldUsePageSizeOneWhenSizeIsLessThanOne() {
            when(hospitalInfoRepository.findByActiveTrue(any(Pageable.class))).thenReturn(Page.empty());

            hospitalInfoService.getAll(0, 0);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(hospitalInfoRepository).findByActiveTrue(pageableCaptor.capture());

            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getByCategory")
    class GetByCategory {
        @Test
        void getByCategory_shouldReturnMappedHospitalInformation() {
            when(hospitalInfoRepository.findByCategoryAndActiveTrue(InformationCategory.EMERGENCY)).thenReturn(List.of(hospitalInfo));

            when(hospitalInfoMapper.toResponseDto(hospitalInfo)).thenReturn(responseDto);

            List<HospitalInfoResponseDto> result = hospitalInfoService.getByCategory(InformationCategory.EMERGENCY);

            assertThat(result).containsExactly(responseDto);

            verify(hospitalInfoRepository).findByCategoryAndActiveTrue(InformationCategory.EMERGENCY);
            verify(hospitalInfoMapper).toResponseDto(hospitalInfo);
        }

        @Test
        @DisplayName("returns empty list when no info matches category")
        void returnsEmptyListWhenNoneMatch() {
            when(hospitalInfoRepository.findByCategoryAndActiveTrue(InformationCategory.GENERAL)).thenReturn(List.of());

            List<HospitalInfoResponseDto> result = hospitalInfoService.getByCategory(InformationCategory.GENERAL);

            assertThat(result).isEmpty();
            verify(hospitalInfoMapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        void update_shouldUpdateSavePublishEventAndReturnResponse() {
            when(hospitalInfoRepository.findById(1L)).thenReturn(Optional.of(hospitalInfo));
            when(hospitalInfoRepository.save(hospitalInfo)).thenReturn(hospitalInfo);
            when(hospitalInfoMapper.toResponseDto(hospitalInfo)).thenReturn(responseDto);

            HospitalInfoResponseDto result = hospitalInfoService.update(1L, requestDto);

            assertThat(result).isSameAs(responseDto);

            verify(hospitalInfoRepository).findById(1L);
            verify(hospitalInfoMapper).updateEntityFromDto(requestDto, hospitalInfo);
            verify(hospitalInfoRepository).save(hospitalInfo);
            verify(eventPublisher).publishEvent(any(HospitalInfoUpdatedEvent.class));
        }


        @Test
        void update_shouldThrowResourceNotFoundWhenHospitalInfoDoesNotExist() {
            when(hospitalInfoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalInfoService.update(99L, requestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("HospitalInfo with id99Not Found");

            verify(hospitalInfoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {
        @Test
        void deactivate_shouldSetActiveFalseSaveAndPublishEvent() {
            hospitalInfo.setActive(true);

            when(hospitalInfoRepository.findById(1L)).thenReturn(Optional.of(hospitalInfo));

            hospitalInfoService.deactivate(1L);

            assertThat(hospitalInfo.getActive()).isFalse();

            verify(hospitalInfoRepository).save(hospitalInfo);
            verify(eventPublisher).publishEvent(any(HospitalInfoDeactivatedEvent.class));
        }

        @Test
        void deactivate_shouldThrowResourceNotFoundWhenHospitalInfoDoesNotExist() {
            when(hospitalInfoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalInfoService.deactivate(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Id with this hospital information not found");

            verify(hospitalInfoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {
        @Test
        void activate_shouldSetActiveTrueSaveAndPublishEvent() {
            hospitalInfo.setActive(false);

            when(hospitalInfoRepository.findById(1L)).thenReturn(Optional.of(hospitalInfo));

            hospitalInfoService.activate(1L);

            assertThat(hospitalInfo.getActive()).isTrue();

            verify(hospitalInfoRepository).save(hospitalInfo);
            verify(eventPublisher).publishEvent(any(HospitalInfoActivateEvent.class));
        }

        @Test
        void activate_shouldThrowResourceNotFoundWhenHospitalInfoDoesNotExist() {
            when(hospitalInfoRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalInfoService.activate(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Id with this hospital information not found");

            verify(hospitalInfoRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }


    @Nested
    @DisplayName("delete")
    class Delete {
    @Test
    void delete_shouldDeleteHospitalInfoAndPublishEvent() {
        when(hospitalInfoRepository.findById(1L)).thenReturn(Optional.of(hospitalInfo));

        hospitalInfoService.delete(1L);

        verify(hospitalInfoRepository).delete(hospitalInfo);
        verify(eventPublisher).publishEvent(any(HospitalInfoDeleteEvent.class));
    }

        @Test
        void delete_shouldThrowResourceNotFoundWhenHospitalInfoDoesNotExist() {
            when(hospitalInfoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalInfoService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Id with this hospital information not found");

            verify(hospitalInfoRepository, never()).delete(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
