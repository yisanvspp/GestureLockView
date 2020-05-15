package com.yisan.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.yisan.library.listener.OnGestureLockListener;
import com.yisan.library.model.Point;
import com.yisan.library.painter.Painter;
import com.yisan.library.painter.System360Painter;
import com.yisan.library.util.DimensionUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 参考开源项目学习自定义view
 * 原作者项目地址：https://github.com/sinawangnan7/GestureLockView
 *
 * @author：wzh
 * @description: 手势解锁
 * @packageName: com.yisan.library
 * @date：2020/5/12 0012 下午 3:40
 */
public class GestureLockView extends View {

    /**
     * 缩放模式(注解)
     */
    @IntDef({NORMAL, REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleMode {
    }

    public static final int NORMAL = 0x0000; // 正常缩放
    public static final int REVERSE = 0x0001; // 反转缩放

    /**
     * 控件的size、控件为正方形
     */
    private int viewSize;
    /**
     * 点半径比例 (不设置默认0.6F)
     */
    private float mRadiusRatio;
    /**
     * 点半径（取值范围[0,viewSize的1/6]，通过{@link GestureLockView#mRadiusRatio}属性进行控制）
     * <p>
     * 注:mRadius代表单位点的可见半径和有效触摸半径，不会随单位点的动画而改变
     */
    private int mRadius;
    /**
     * 点列表容器（用于记录已被按下的点）
     */
    private final List<Point> mPressPoints = new ArrayList<>(9);
    /**
     * 点的数组矩阵
     */
    private Point[][] mPoints = new Point[3][3];
    /**
     * 正常状态的画笔
     */
    private Paint normalPaint;
    /**
     * 绘制者
     */
    private Painter mPainter = new System360Painter();
    /**
     * 正常 & 按下 & 错误等状态下画笔的颜色值
     */
    private int mNormalColor;
    private int mPressColor;
    private int mErrorColor;

    /**
     * 线的粗细值（不设置则默认为1dp）
     */
    private int mLineThickness;

    /**
     * 记录当前视图是否处于错误状态
     */
    private boolean isErrorStatus;

    /**
     * 点动画列表
     */
    private final List<ValueAnimator> mPointAnimators = new ArrayList<>(9);

    /**
     * 动画缩放模式
     */
    private int mAnimationScaleMode = NORMAL;

    /**
     * 动画缩放比例（不设置默认1.5F）
     */
    private float mAnimationScaleRate;
    /**
     * 振动器
     */
    private Vibrator mVibrator;
    /**
     * 震动持续时间（不设置默认40毫秒）
     */
    private long mVibrateDuration;
    /**
     * 动画时长
     */
    private long mAnimationDuration;

    public GestureLockView(Context context) {
        this(context, null);
    }

    public GestureLockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initPainter() {
        // 使Painter关联当前手势解锁视图
        mPainter.attach(this, getContext(),
                mNormalColor, mPressColor, mErrorColor);
    }

    /**
     * 初始化属性
     *
     * @param context 上下文环境
     * @param attrs   XML属性信息集
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        // 1.初始化XML属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GestureLockView);
        mRadiusRatio = array.getFloat(R.styleable.GestureLockView_radius_ratio, 0.6F);
        mLineThickness = array.getDimensionPixelSize(R.styleable.GestureLockView_line_thickness, DimensionUtil.dp2px(context, 1));
        mNormalColor = array.getColor(R.styleable.GestureLockView_normal_color, Painter.NORMAL_COLOR);
        mPressColor = array.getColor(R.styleable.GestureLockView_press_color, Painter.PRESS_COLOR);
        mErrorColor = array.getColor(R.styleable.GestureLockView_error_color, Painter.ERROR_COLOR);
        mAnimationDuration = array.getInt(R.styleable.GestureLockView_animation_duration, 200);
        mAnimationScaleMode = array.getInt(R.styleable.GestureLockView_animation_scale_mode, NORMAL);
        mAnimationScaleRate = array.getFloat(R.styleable.GestureLockView_animation_scale_rate, 1.5F);
        mVibrateDuration = array.getInt(R.styleable.GestureLockView_vibrate_duration, 40);
        array.recycle();
        // 2.修正部分参数（防止参数越界）
        mRadiusRatio = (mRadiusRatio < 0) ? 0 : mRadiusRatio > 1 ? 1 : mRadiusRatio;
        mAnimationScaleRate = mAnimationScaleRate < 0 ? 0 : mAnimationScaleRate;
    }


    /**
     * 初始化3*3数组
     */
    private void initPointArray() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point point = new Point();
                point.x = viewSize / 3 / 2 * (j * 2 + 1);
                point.y = viewSize / 3 / 2 * (i * 2 + 1);
                point.radius = mRadius;
                point.status = Point.POINT_NORMAL_STATUS;
                point.index = i * 3 + j;
                mPoints[i][j] = point;
            }
        }
    }

    /**
     * 初始化参数
     */
    private void initParams() {
        //设置点的半径
        mRadius = (int) (viewSize / 6 * mRadiusRatio);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        normalPaint = new Paint();
        normalPaint.setStrokeWidth(dp2px(5));
        normalPaint.setColor(Color.BLACK);
        normalPaint.setAntiAlias(true);
        normalPaint.setDither(true);
        normalPaint.setStyle(Paint.Style.STROKE);
    }

    private int dp2px(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //view取最小的边。显示成正方形
        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mHeight = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = Math.min(mWidth, mHeight);
        setMeasuredDimension(viewSize, viewSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //初始化参数
        initParams();
        //初始化画笔
        initPaint();
        //初始化3*3点数组
        initPointArray();
        //初始化绘制者
        initPainter();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        //绘制点
        mPainter.drawPoints(mPoints, canvas);
        //绘制线
        mPainter.drawLines(mPressPoints, mEventX, mEventY, mLineThickness, canvas);

    }

    /**
     * 按下的x，y坐标
     */
    private float mEventX;
    private float mEventY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mEventX = event.getX();
        mEventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downEventDeal(mEventX, mEventY);
                break;
            case MotionEvent.ACTION_MOVE:
                moveEventDeal(mEventX, mEventY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                upEventDeal();
                break;
            default:
                break;
        }
        // 2.重绘
        postInvalidate();
        return true;
    }

    /**
     * ACTION_UP/ACTION_CANCEL事件处理方法
     */
    private void upEventDeal() {
        // 1.回调手势解锁监听器Complete方法
        if (mOnGestureLockListener != null) {
            mOnGestureLockListener.onComplete(getPassword());
        }
        // 2.清除触摸点到最后按下单元点的连线
        if (!mPressPoints.isEmpty()) {
            mEventX = mPressPoints.get(mPressPoints.size() - 1).x;
            mEventY = mPressPoints.get(mPressPoints.size() - 1).y;
        }
        // 3.提前结束未执行完的动画
        if (!mPointAnimators.isEmpty()) {
            for (ValueAnimator animator : mPointAnimators) {
                animator.end();
            }
            mPointAnimators.clear();
        }
        // 4.重绘
        postInvalidate();
    }


    /**
     * ACTION_MOVE事件处理方法
     *
     * @param eventX 事件X坐标（相对于GestureLockView）
     * @param eventY 事件Y坐标（相对于GestureLockView）
     */
    private void moveEventDeal(float eventX, float eventY) {
        // 1.修改点状态
        modifyPointStatus(eventX, eventY);
    }

    /**
     * 按下事件处理
     *
     * @param mEventX x
     * @param mEventY y
     */
    private void downEventDeal(float mEventX, float mEventY) {
        //1、回调手势解锁监听器onStarted方法
        if (mOnGestureLockListener != null) {
            mOnGestureLockListener.onStarted();
        }
        //2、清理之前的绘制信息
        clear();
        //3、修改点状态
        modifyPointStatus(mEventX, mEventY);

        isErrorStatus = false;
    }

    /**
     * 获取手势密码（手势图案以数字密码形式返回）
     */
    private String getPassword() {
        StringBuilder builder = new StringBuilder();
        for (Point pressPoint : mPressPoints) {
            builder.append(pressPoint.index);
        }
        return builder.toString();
    }


    /**
     * 根据触摸事件修改点的状态
     *
     * @param x x
     * @param y y
     */
    private void modifyPointStatus(float x, float y) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point point = mPoints[i][j];
                float dx = Math.abs(x - point.x);
                float dy = Math.abs(y - point.y);
                //按下的位置距离点圆心的距离
                int downRadius = (int) Math.sqrt(dx * dx + dy * dy);
                if (downRadius < mRadius) {
                    //按下的点唯一point半径以内
                    point.status = Point.POINT_PRESS_STATUS;
                    addPressPoint(point);
                    return;
                }
            }
        }
    }


    /**
     * 添加按下的点
     *
     * @param point 点对象
     */
    private void addPressPoint(Point point) {
        // 1.判断该点是否之前已添加过
        if (mPressPoints.contains(point)) {
            return;
        }
        // 2.如果两点之间还有点没添加,先添加中间点
        if (!mPressPoints.isEmpty()) {
            addMiddlePoint(point);
        }
        // 3.添加按下的点
        mPressPoints.add(point);
        // 4.开启动画
        startAnimation(point, 300);
        // 5.开启震动
        if (mVibrator == null) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(mVibrateDuration);

        // 6.回调手势解锁监听器的onPregress方法
        if (mOnGestureLockListener != null) {
            mOnGestureLockListener.onProgress(getPassword());
        }
    }


    /**
     * 开启动画
     *
     * @param point    单位点
     * @param duration 持续时长
     */
    private void startAnimation(final Point point, long duration) {
        ValueAnimator valueAnimator;
        // 2.判断动画缩放模式，采用不同策略的属性动画
        if (mAnimationScaleMode == 1) {
            valueAnimator = ValueAnimator.ofInt(point.radius, (int) (mAnimationScaleRate * point.radius), point.radius);
        } else {
            valueAnimator = ValueAnimator.ofInt((int) (mAnimationScaleRate * point.radius), point.radius);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                point.radius = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.start();
        // 3.添加ValueAnimator至动画列表
        mPointAnimators.add(valueAnimator);
    }


    /**
     * 添加中间点（判断两点之间是否存在中间点，如果有还没有添加则先行添加进来）
     *
     * @param point 点对象
     */
    private void addMiddlePoint(Point point) {
        Point lastPoint = mPressPoints.get(mPressPoints.size() - 1);
        // 1.判断两个点是否是同一个点
        if (lastPoint == point) {
            return;
        }
        // 2.判断两点之间是否存在中间点
        int middleX = (lastPoint.x + point.x) / 2;
        int middleY = (lastPoint.y + point.y) / 2;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point tempPoint = mPoints[i][j];
                int dx = Math.abs(tempPoint.x - middleX);
                int dy = Math.abs(tempPoint.y - middleY);
                if (Math.sqrt(dx * dx + dy * dy) < mRadius) {
                    // 3.开启递归调用
                    tempPoint.status = Point.POINT_PRESS_STATUS;
                    addPressPoint(tempPoint);
                    return;
                }
            }
        }
    }


    /**
     * 清理数据至初始状态
     */
    private void clear() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mPoints[i][j].status = Point.POINT_NORMAL_STATUS;
                mPoints[i][j].radius = mRadius;
            }
        }
        mPressPoints.clear();
    }


    //---------------------------------------set --------------------------------------------

    /**
     * 显示错误状态 (当没有按下的点时，使用该方法无效的)
     */
    public void showErrorStatus() {
        isErrorStatus = true;
        for (Point point : mPressPoints) {
            point.status = Point.POINT_ERROR_STATUS;
        }
        postInvalidate();
    }

    /**
     * 显示错误状态（持续millisecond毫秒后还原至初始状态）
     *
     * @param millisecond 持续时间
     */
    public void showErrorStatus(long millisecond) {
        showErrorStatus();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isErrorStatus) {
                    clearView();
                }
            }
        }, millisecond);
    }

    /**
     * 清理视图至初始状态
     */
    public void clearView() {
        post(new Runnable() {
            @Override
            public void run() {
                clear();
                postInvalidate();
            }
        });
    }

    /**
     * 解锁监听器
     */
    private OnGestureLockListener mOnGestureLockListener;

    public void setOnGestureLockListener(OnGestureLockListener listener) {
        this.mOnGestureLockListener = listener;
    }

    /**
     * 获取半径值（View执行完onSizeChanged(w, h, oldw, oldh)方法后mRadius才有值）
     */
    public int getRadius() {
        return mRadius;
    }
}
