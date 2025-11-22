package com.musicstyle.service;

import com.musicstyle.dto.SpotifyUserResponse;
import com.musicstyle.model.entity.User;
import com.musicstyle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SpotifyUserService {

    private final WebClient spotifyWebClient;
    private final UserRepository userRepository;

    public User fetchAndSaveSpotifyUser(String accessToken) {
        SpotifyUserResponse response = spotifyWebClient.get()
                .uri("/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(SpotifyUserResponse.class)
                .block();

        User user = userRepository.findBySpotifyId(response.getId())
                .orElse(new User());

        user.setSpotifyId(response.getId());
        user.setDisplayName(response.getDisplayName());
        user.setEmail(response.getEmail());
        user.setProfileUrl(response.getExternalUrls().get("spotify"));
        user.setImageUrl(response.getImages() != null && !response.getImages().isEmpty() ? response.getImages().get(0).getUrl() : null);
        user.setCountry(response.getCountry());
        user.setProduct(response.getProduct());
        user.setFollowers(response.getFollowers().getTotal());

        return userRepository.save(user);
    }
}