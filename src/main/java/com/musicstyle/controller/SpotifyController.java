package com.stylist.controller;

import com.stylist.model.Music;
import org.springframework.beans.factory.annotation.Autowired;
import com.stylist.service.SpotifyUserService;
import com.stylist.service.SpotifyAuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class SpotifyController {

    @Autowired
    private final SpotifyUserService spotifyUserService;

    @Autowired
    private final SpotifyAuthService spotifyAuthService;

    public SpotifyController(SpotifyUserService spotifyUserService) {
        this.spotifyUserService = spotifyUserService;
    }

    @GetMapping("/music/{styleName}")
    public List<Music> getMusicByStyle(@PathVariable String styleName) {
       // return spotifyUserService.findMusicByStyle(styleName);
     return ResponseEntity.ok("Busca por estilo em desenvolvimento");

    }
}
