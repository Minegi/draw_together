package com.samsung.hackathon.drawtogether.model;

import android.graphics.PointF;
import android.graphics.RectF;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "m")
public class StrokeModel implements Serializable {
    private int mUserId;
    private String mDefaultePenName;
    private String mPenName;
    private float mPenSize;
    private int mColor;
    private String mAdvancedPenSetting;
    private float[] mXPoints;
    private float[] mYPoints;
    private float[] mPressures;
    private int[] mTimeStamps;
    private float[] mTilts;
    private float[] mOrientations;
    private int mToolType;
    private float mRotation;
    private int mResizeOption;
    // left, top, right, bottom 순서대로 저장
    private float[] mRect;
}
