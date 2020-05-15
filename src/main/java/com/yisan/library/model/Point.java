package com.yisan.library.model;

/**
 * @author：wzh
 * @description: 点
 * @packageName: com.yisan.library.model
 * @date：2020/5/12 0012 下午 3:47
 */
public class Point {
    /**
     * 正常状态
     */
    public static final int POINT_NORMAL_STATUS = 0x0001;
    /**
     * 按下状态
     */
    public static final int POINT_PRESS_STATUS = 0x0002;
    /**
     * 出错状态
     */
    public static final int POINT_ERROR_STATUS = 0x0003;
    /**
     * x坐标
     */
    public int x ;
    /**
     * y坐标
     */
    public int y ;
    /**
     * 半径
     */
    public int radius;
    /**
     * 点的状态
     */
    public int status;
    /**
     * 点下标 (取值范围[0,8]，用于解锁完成后把手势密码转换成数字密码)
     */
    public int index;


}
