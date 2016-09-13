package com.samsung.hackathon.drawtogether.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public abstract class Drawable {
    protected int seq;
    protected ArrayList<Point> points;
}
