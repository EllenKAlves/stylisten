package com.stylisten.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "genre_stats", indexes = {
    @Index(name = "idx_user_period", columnList = "user_id, period_start")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class GenreStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "genre_name", nullable = false)
    private String genreName;

    @Column(name = "raw_count", nullable = false)
    private Integer rawCount;

    @Column(name = "normalized_score", nullable = false)
    private Double normalizedScore;

    @Column(name = "period_start", nullable = false)
    private java.time.LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private java.time.LocalDate periodEnd;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}