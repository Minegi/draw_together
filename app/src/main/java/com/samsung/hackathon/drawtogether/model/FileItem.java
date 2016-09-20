package com.samsung.hackathon.drawtogether.model;

/**
 * Created by Hi.JiGOO on 16. 9. 14..
 */
public class FileItem {
    public String name;
    public String file;
    public String thumbnail;

    public FileItem(String name) {
        this.name = name;
    }
    public FileItem(String name, String file, String thumbnail) {
        this.name = name;
        this.file = file;
        this.thumbnail = thumbnail;
    }
}
