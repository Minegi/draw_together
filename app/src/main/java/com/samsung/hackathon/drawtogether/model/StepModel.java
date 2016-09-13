package com.samsung.hackathon.drawtogether.model;


import java.util.ArrayList;

import lombok.Data;

@Data
public class StepModel {
    private int seq;
    private ArrayList<Drawable> strokes;
}
