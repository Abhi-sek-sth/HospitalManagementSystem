package org.example.hospitalmanagementsystem.repository;

import org.example.hospitalmanagementsystem.entity.HospitalInfo;
import org.example.hospitalmanagementsystem.entity.InformationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface HospitalInfoRepository   extends JpaRepository<HospitalInfo, Long> {

    List<HospitalInfo> findByCategoryAndActiveTrue(InformationCategory category);
    Page<HospitalInfo> findByActiveTrue(Pageable pageable);

}
