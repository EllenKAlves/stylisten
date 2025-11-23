package com.stylist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_music_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserMusicProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_genre_frequencies", 
                     joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "genre")
    @Column(name = "frequency")
    private Map<String, Integer> genreFrequency; 
    // ex: {"pop": 15, "rock": 10, "indie": 8}

    @Column
    private Instant analyzedAt;

    @Column
    private Integer totalTracksAnalyzed;

    @PrePersist
    protected void onCreate() {
        analyzedAt = Instant.now();
    }
}