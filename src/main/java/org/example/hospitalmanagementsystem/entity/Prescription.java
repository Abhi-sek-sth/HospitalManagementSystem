package org.example.hospitalmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted = false")
@Table(
        name = "prescriptions",
        indexes = {
                @Index(name = "idx_prescription_medical_record", columnList = "medical_record_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "medical_record_id",
            nullable = false
    )
    private MedicalRecords medicalRecord;

    @Column(length = 2000)
    private String instructions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime prescribedAt;

    @PrePersist
    protected void onCreate() {
        prescribedAt = LocalDateTime.now();
    }

    @Version
    private Long version;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    private Long deletedBy;
}