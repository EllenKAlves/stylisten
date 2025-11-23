package com.stylist.repository;

import com.stylist.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   
     Optional<User> findBySpotifyId(String spotifyId); 
     
}