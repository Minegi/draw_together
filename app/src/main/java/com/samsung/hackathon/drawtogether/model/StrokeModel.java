package com.samsung.hackathon.drawtogether.model;

import lombok.Data;

@Data
public class StrokeModel extends Drawable {
    private String penType;
    private int color;
    private float size;
    private String advancedSetting;
}
