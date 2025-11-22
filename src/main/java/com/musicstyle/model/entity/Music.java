package com.stylist.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Music {
    private String title;
    private String artist;
    private String url;
}
