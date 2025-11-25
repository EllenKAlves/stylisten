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
public interface StyleRepository extends JpaRepository<Style, UUID> {
    Optional<Style> findByName(String name);
    
    @Query("SELECT s FROM Style s WHERE :tag = ANY(s.tags)")
    List<Style> findByTagsContaining(@Param("tag") String tag);
}