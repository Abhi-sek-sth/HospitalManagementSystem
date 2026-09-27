package org.example.hospitalmanagementsystem.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.hospitalmanagementsystem.dto.request.HospitalInfoRequestDto;
import org.example.hospitalmanagementsystem.dto.response.HospitalInfoResponseDto;
import org.example.hospitalmanagementsystem.dto.response.PageResponseDto;
import org.example.hospitalmanagementsystem.entity.HospitalInfo;
import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.mapper.HospitalInfoMapper;
import org.example.hospitalmanagementsystem.repository.HospitalInfoRepository;
import org.example.hospitalmanagementsystem.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalInfoServiceImpl implements HospitalInfoService {
    private final HospitalInfoMapper hospitalInfoMapper;
    private final HospitalInfoRepository hospitalInfoRepository;
    private final VectorIndexService vectorIndexService;
    private static final int MAX_PAGE_SIZE = 100;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    @Transactional
    public HospitalInfoResponseDto create(HospitalInfoRequestDto requestDto) {

        HospitalInfo hospitalInfo = hospitalInfoMapper.toEntity(requestDto);
        hospitalInfo.setActive(true);
        HospitalInfo savedHospitalInfo = hospitalInfoRepository.save(hospitalInfo);
        eventPublisher.publishEvent(
                new HospitalInfoCreatedEvent(savedHospitalInfo.getId(),savedHospitalInfo.getCategory(),savedHospitalInfo.getTitle(),savedHospitalInfo.getDescription())
        );

        return hospitalInfoMapper.toResponseDto(savedHospitalInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalInfoResponseDto getById(Long id) {
        HospitalInfo information = hospitalInfoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hospital information not found with id: " + id));

        return hospitalInfoMapper.toResponseDto(information);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<HospitalInfoResponseDto> getAll(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("createdAt").descending());

        Page<HospitalInfo> hospitalInfos = hospitalInfoRepository.findByActiveTrue(pageable);

        return new PageResponseDto<>(
                hospitalInfos.getContent()
                        .stream()
                        .map(hospitalInfoMapper::toResponseDto)
                        .toList(),
                hospitalInfos.getNumber(),
                hospitalInfos.getTotalPages(),
                hospitalInfos.getTotalElements()


        );
    }
    @Override
    @Transactional(readOnly = true)
    public List<HospitalInfoResponseDto> getByCategory(InformationCategory category) {
        List<HospitalInfo> infos = hospitalInfoRepository.findByCategoryAndActiveTrue(category);
        return infos.stream()
                .map(hospitalInfoMapper::toResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public HospitalInfoResponseDto update(Long id, HospitalInfoRequestDto requestDto) {
        HospitalInfo hospitalInfo = hospitalInfoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HospitalInfo with id" + id + "Not Found"));
       try {
           hospitalInfoMapper.updateEntityFromDto(requestDto, hospitalInfo);
       }catch (Exception e){
           log.info("Not updated",e);
       }
        HospitalInfo updated = hospitalInfoRepository.save(hospitalInfo);

        eventPublisher.publishEvent(
                new HospitalInfoUpdatedEvent(updated.getId(),updated.getCategory(),updated.getTitle(),updated.getDescription()));

        return hospitalInfoMapper.toResponseDto(updated);
    }
    @Transactional
    @Override
    public void deactivate(Long id) {
        HospitalInfo hospitalInfo = hospitalInfoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id with this hospital information not found"));

        hospitalInfo.setActive(false);
        hospitalInfoRepository.save(hospitalInfo);

        eventPublisher.publishEvent(
                new HospitalInfoDeactivatedEvent( hospitalInfo.getId(),
                        hospitalInfo.getCategory(),
                        hospitalInfo.getTitle(),
                        hospitalInfo.getDescription())
        );

        log.info("Deactivated hospital info id={}", id);
}
    @Transactional
    @Override
    public void activate(Long id) {
        HospitalInfo hospitalInfo = hospitalInfoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id with this hospital information not found"));

        hospitalInfo.setActive(true);
        hospitalInfoRepository.save(hospitalInfo);

        eventPublisher.publishEvent(
                new HospitalInfoActivateEvent( hospitalInfo.getId(),
                        hospitalInfo.getCategory(),
                        hospitalInfo.getTitle(),
                        hospitalInfo.getDescription())
        );

        log.info("Activate hospital info id={}", id);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        HospitalInfo hospitalInfo = hospitalInfoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Id with this hospital information not found"));
        hospitalInfoRepository.delete(hospitalInfo);
        eventPublisher.publishEvent(
                new HospitalInfoDeleteEvent(hospitalInfo.getId())
        );
        log.info("Delete hospital info id={}", id);
    }
}
