package com.stylisten.domain.repository;

import com.stylisten.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreStyleMappingRepository extends JpaRepository<GenreStyleMapping, UUID> {
    List<GenreStyleMapping> findByGenreName(String genreName);
    
    @Query("SELECT gsm FROM GenreStyleMapping gsm " +
           "WHERE gsm.genreName IN :genres " +
           "ORDER BY gsm.weight DESC")
    List<GenreStyleMapping> findByGenreNameIn(@Param("genres") List<String> genres);
    
    @Query("SELECT DISTINCT gsm.genreName FROM GenreStyleMapping gsm")
    List<String> findAllDistinctGenres();
}