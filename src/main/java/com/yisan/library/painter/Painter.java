package com.yisan.library.painter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.yisan.library.GestureLockView;
import com.yisan.library.model.Point;

import java.util.List;

/**
 * @author：wzh
 * @description: 绘制者
 * @packageName: com.yisan.library.painter
 * @date：2020/5/13 0013 上午 11:03
 */
public abstract class Painter {

    private Context mContext;

    // 正常状态画笔颜色
    public static final int NORMAL_COLOR = Color.GRAY;
    // 按压状态画笔颜色
    public static final int PRESS_COLOR = Color.BLACK;
    // 出错状态画笔颜色
    public static final int ERROR_COLOR = Color.RED;

    /**
     * 正常 & 按下 & 错误状态画笔
     */
    protected final Paint normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint pressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 手势解锁视图
     */
    private GestureLockView mGestureLockView;


    /**
     * 关联手势解锁视图
     *
     * @param gestureLockView 手势解锁视图
     * @param context         上下文环境
     * @param normalColor     正常状态画笔颜色
     * @param pressColor      按下状态画笔颜色
     * @param errorColor      错误状态画笔颜色
     */
    public void attach(GestureLockView gestureLockView, Context context,
                       int normalColor, int pressColor, int errorColor) {
        // 1.关联手势解锁视图
        mGestureLockView = gestureLockView;
        mContext = context;
        // 2.设置Painter画笔颜色
        setNormalColor(normalColor);
        setPressColor(pressColor);
        setErrorColor(errorColor);
    }

    /**
     * 设置出错状态画笔颜色
     *
     * @param errorColor 出错状态画笔颜色 (具体颜色值,不是引用值)
     */
    private void setErrorColor(int errorColor) {
        errorPaint.setColor(errorColor);
    }

    /**
     * 设置按下状态画笔颜色
     *
     * @param pressColor 按下状态画笔颜色 (具体颜色值,不是引用值)
     */
    private void setPressColor(int pressColor) {
        pressPaint.setColor(pressColor);
    }

    /**
     * 设置正常状态画笔颜色
     *
     * @param normalColor 正常状态画笔颜色 (具体颜色值,不是引用值)
     */
    private void setNormalColor(int normalColor) {
        normalPaint.setColor(normalColor);
    }

    /**
     * 3x3点绘制方法
     *
     * @param points 3x3点数组
     * @param canvas 画布
     */
    public void drawPoints(Point[][] points, Canvas canvas) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point point = points[i][j];
                switch (point.status) {
                    case Point.POINT_NORMAL_STATUS:
                        drawNormalPoint(point, canvas, normalPaint);
                        break;
                    case Point.POINT_PRESS_STATUS:
                        drawPressPoint(point, canvas, pressPaint);
                        break;
                    case Point.POINT_ERROR_STATUS:
                        drawErrorPoint(point, canvas, errorPaint);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    /**
     * 画错误的点
     */
    public abstract void drawErrorPoint(Point point, Canvas canvas, Paint errorPaint);

    /**
     * 画按下的点
     */
    public abstract void drawPressPoint(Point point, Canvas canvas, Paint pressPaint);

    /**
     * 画初始状态的点
     */
    public abstract void drawNormalPoint(Point point, Canvas canvas, Paint normalPaint);

    /**
     * 绘制连线
     *
     * @param points   点集合（已被按下的点）
     * @param eventX   事件X坐标（当前触摸位置）
     * @param eventY   事件Y坐标（当前触摸位置）
     * @param lineSize 线的粗细值
     * @param canvas   画布
     */
    public void drawLines(List<Point> points, float eventX, float eventY, int lineSize, Canvas canvas) {
        // 1.参数合法性判断
        if (points.size() <= 0) {
            return;
        }
        // 2.根据点列表生成连线路径
        Path path = generateLinePath(points, eventX, eventY);
        // 3.区分点的状态，使用不同画笔绘制连线
        switch (points.get(0).status) {
            // 按下状态
            case Point.POINT_PRESS_STATUS:
                Paint pressPaint = new Paint();
                pressPaint.setColor(PRESS_COLOR);
                pressPaint.setStyle(Paint.Style.STROKE);
                pressPaint.setStrokeWidth(lineSize);
                canvas.drawPath(path, pressPaint);
                break;
            // 出错状态
            case Point.POINT_ERROR_STATUS:
                Paint errorPaint = new Paint();
                errorPaint.setColor(ERROR_COLOR);
                errorPaint.setStyle(Paint.Style.STROKE);
                errorPaint.setStrokeWidth(lineSize);
                canvas.drawPath(path, errorPaint);
                break;
            default:
                break;
        }
    }

    /**
     * 生成连线路径
     *
     * @param points 点集合（已被按下记录的点）
     * @param eventX 事件X坐标（当前触摸位置）
     * @param eventY 事件Y坐标（当前触摸位置）
     */
    private Path generateLinePath(List<Point> points, float eventX, float eventY) {
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (i == 0) {
                path.moveTo(point.x, point.y);
            } else {
                path.lineTo(point.x, point.y);
            }
        }
        path.lineTo(eventX, eventY);
        return path;
    }

    /**
     * 获取手势解锁视图
     */
    public GestureLockView getGestureLockView(){
        return mGestureLockView;
    }
}
