package com.stylist.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "fashion_styles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FashionStyle {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // ex: "Streetwear", "Minimalista", "Boho"

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "fashion_style_genres", 
                     joinColumns = @JoinColumn(name = "style_id"))
    @Column(name = "music_genre")
    private Set<String> associatedMusicGenres; 
    // ex: ["hip hop", "trap", "rap"] para Streetwear

    @Column
    private Integer weight; // Peso de cada gênero na pontuação (0-100)

    @Column(length = 500)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "fashion_style_keywords", 
                     joinColumns = @JoinColumn(name = "style_id"))
    @Column(name = "keyword")
    private Set<String> keywords; 
    // ex: ["urban", "oversized", "sneakers"] para Streetwear
}