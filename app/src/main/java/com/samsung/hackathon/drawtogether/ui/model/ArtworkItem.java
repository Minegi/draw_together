package com.samsung.hackathon.drawtogether.ui.model;

/**
 * Created by Hi.JiGOO on 16. 9. 14..
 */
public class ArtworkItem {
    public String name;
    public String strokeData;
    public String thumbnail;

    public ArtworkItem(String name) {
        this.name = name;
    }
    public ArtworkItem(String name, String strokeData, String thumbnail) {
        this.name = name;
        this.strokeData = strokeData;
        this.thumbnail = thumbnail;
    }
}
