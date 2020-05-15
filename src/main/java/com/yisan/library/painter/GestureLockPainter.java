package com.yisan.library.painter;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.yisan.library.model.Point;

/**
 * @author：wzh
 * @description: 默认的Painter
 * @packageName: com.yisan.library.painter
 * @date：2020/5/15 0015 上午 10:28
 */
public class GestureLockPainter extends Painter {

    @Override
    public void drawErrorPoint(Point point, Canvas canvas, Paint errorPaint) {
        // 1.记录画笔的原始属性（绘制过程中需要修改的属性）,绘制结束时进行还原
        Paint.Style style = errorPaint.getStyle();
        float originStrokeWidth = errorPaint.getStrokeWidth();
        // 2.绘制实心点
        errorPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point.x, point.y, point.radius / 3, errorPaint);
        // 3.绘制空心圆
        errorPaint.setStyle(Paint.Style.STROKE);
        errorPaint.setStrokeWidth(point.radius / 16);
        canvas.drawCircle(point.x, point.y, point.radius, errorPaint);
        // 4.结束绘制，还原画笔属性
        errorPaint.setStyle(style);
        errorPaint.setStrokeWidth(originStrokeWidth);
    }

    @Override
    public void drawPressPoint(Point point, Canvas canvas, Paint pressPaint) {
        // 1.记录画笔的原始属性（绘制过程中需要修改的属性）,绘制结束时进行还原
        Paint.Style style = pressPaint.getStyle();
        float originStrokeWidth = pressPaint.getStrokeWidth();
        // 2.绘制实心点
        pressPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point.x, point.y, point.radius / 3, pressPaint);
        // 3.绘制空心圆边界
        pressPaint.setStyle(Paint.Style.STROKE);
        pressPaint.setStrokeWidth(point.radius / 16);
        canvas.drawCircle(point.x, point.y, point.radius, pressPaint);
        // 4.结束绘制，还原画笔属性
        pressPaint.setStyle(style);
        pressPaint.setStrokeWidth(originStrokeWidth);
    }

    @Override
    public void drawNormalPoint(Point point, Canvas canvas, Paint normalPaint) {
        // 1.记录画笔的原始属性（绘制过程中需要修改的属性）,绘制结束时进行还原
        Paint.Style style = normalPaint.getStyle();
        float originStrokeWidth = normalPaint.getStrokeWidth();
        // 2.绘制空心圆边界
        normalPaint.setStyle(Paint.Style.STROKE);
        normalPaint.setStrokeWidth(point.radius / 32);
        canvas.drawCircle(point.x, point.y, point.radius, normalPaint);
        // 3.结束绘制，还原画笔属性
        normalPaint.setStyle(style);
        normalPaint.setStrokeWidth(originStrokeWidth);
    }
}
