package com.samsung.hackathon.drawtogether.util;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;

import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.hackathon.drawtogether.App;
import com.samsung.hackathon.drawtogether.model.StrokeModel;

public class StrokeModelConvertUtils {
    public static StrokeModel convertToStrokeModel(final SpenObjectStroke spenStroke) {
        final StrokeModel stroke = new StrokeModel();

        stroke.setUserId(spenStroke.getUserId());
        stroke.setDefaultePenName(spenStroke.getDefaultPenName());
        stroke.setPenName(spenStroke.getPenName());
        stroke.setPenSize(spenStroke.getPenSize());
        stroke.setColor(spenStroke.getColor());
        stroke.setAdvancedPenSetting(spenStroke.getAdvancedPenSetting());
        stroke.setXPoints(spenStroke.getXPoints().clone());
        stroke.setYPoints(spenStroke.getYPoints().clone());
        stroke.setPressures(spenStroke.getPressures().clone());
        stroke.setTimeStamps(spenStroke.getTimeStamps().clone());
        if (spenStroke != null && spenStroke.getTilts() != null) {
            stroke.setTilts(spenStroke.getTilts().clone());
        }
        if (spenStroke != null && spenStroke.getOrientations() != null) {
            stroke.setOrientations(spenStroke.getOrientations().clone());
        }
        stroke.setToolType(spenStroke.getToolType());
        stroke.setRotation(spenStroke.getRotation());
        stroke.setResizeOption(spenStroke.getResizeOption());

        final RectF rectF = spenStroke.getRect();

        stroke.setRect(new float[] {rectF.left, rectF.top, rectF.right, rectF.bottom});

        return stroke;
    }

    // TODO: [Low] 모델 객체의 크기가 커지면 시간이 오래 걸릴 수 있음. 그러면 쓰레드로 작업해야 함
    public static SpenObjectStroke convertToSpenObjectStroke(final StrokeModel stroke) {
        final SpenObjectStroke spenStroke = new SpenObjectStroke();

        spenStroke.setUserId(stroke.getUserId());
        spenStroke.setDefaultPenName(stroke.getDefaultePenName());
        spenStroke.setPenName(stroke.getPenName());
        spenStroke.setPenSize(stroke.getPenSize());
        spenStroke.setColor(stroke.getColor());
        spenStroke.setAdvancedPenSetting(stroke.getAdvancedPenSetting());

        final PointF[] points = new PointF[stroke.getXPoints().length];

        App.L.d("points.length=" + points.length);
        final float[] xPoints = stroke.getXPoints();
        final float[] yPoints = stroke.getYPoints();

        for (int i = 0; i < points.length; ++i) {
            if (points[i] == null) {
                points[i] = new PointF();
            }
            points[i].x = xPoints[i];
            points[i].y = yPoints[i];
        }

        spenStroke.setPoints(points, stroke.getPressures(),
                stroke.getTimeStamps(), stroke.getTilts(), stroke.getOrientations());
        spenStroke.setToolType(stroke.getToolType());
        spenStroke.setRotation(stroke.getRotation());
        spenStroke.setResizeOption(stroke.getResizeOption());

        final float[] rectPoints = stroke.getRect();
        spenStroke.setRect(new RectF(rectPoints[0], rectPoints[1],
                rectPoints[2], rectPoints[3]), false);

        return spenStroke;
    }
}
