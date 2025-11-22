package com.stylist.controller;

import com.stylist.model.Music;
import com.stylist.service.SpotifyUserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final SpotifyUserService spotifyService;

    public MusicController(SpotifyUserService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/style/{styleName}")
    public List<Music> getMusicByStyle(@PathVariable String styleName) {
        return spotifyService.findMusicByStyle(styleName);
    }
}
