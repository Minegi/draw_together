package com.samsung.hackathon.drawtogether.model;


import com.samsung.android.sdk.pen.document.SpenObjectStroke;

import java.util.ArrayList;

import lombok.Data;

@Data
public class StepModel {
    private int seq;
    private ArrayList<SpenObjectStroke> strokes;


}
