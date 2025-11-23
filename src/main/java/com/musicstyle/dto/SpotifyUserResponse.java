package com.stylist.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SpotifyUserResponse {
    private String id;
    private String displayName;
    private String email;
    private Map<String, String> externalUrls;
    private List<Image> images;
    private String country;
    private String product;
    private Followers followers;

    @Data
    public static class Image {
        private String url;
    }

    @Data
    public static class Followers {
        private int total;
    }
}
