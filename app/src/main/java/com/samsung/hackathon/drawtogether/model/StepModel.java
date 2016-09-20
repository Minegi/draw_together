package com.samsung.hackathon.drawtogether.model;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;

@Data
public class StepModel implements Serializable {
    private int seq;
    private ArrayList<StrokeModel> strokes;
}
