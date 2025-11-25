package com.stylisten.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracks_cache", indexes = {
    @Index(name = "idx_user_played_at", columnList = "user_id, played_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class TrackCache {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "spotify_track_id", nullable = false)
    private String spotifyTrackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "genres", columnDefinition = "jsonb")
    private String genres;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}