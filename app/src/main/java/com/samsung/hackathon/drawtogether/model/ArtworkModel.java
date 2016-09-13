package com.samsung.hackathon.drawtogether.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class ArtworkModel {
    private int seq;
    private ArrayList<StepModel> steps;
}
