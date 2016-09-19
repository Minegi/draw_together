package com.samsung.hackathon.drawtogether.ui.model;

import android.graphics.drawable.Drawable;

/**
 * Created by 김시내 on 2016-09-17.
 */
public class ArtworkItem {
    private Drawable iconDrawable;
    private String titleStr;

    public String getTitle() {
        return titleStr;
    }

    public void setTitle(String titleStr) {
        this.titleStr = titleStr;
    }

    public Drawable getIcon() {
        return iconDrawable;
    }

    public void setIcon(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }
}
