package com.yisan.library.listener;

/**
 * @author：wzh
 * @description: 手势解锁监听器
 * @packageName: com.yisan.library.listener
 * @date：2020/5/14 0014 下午 3:19
 */
public interface OnGestureLockListener {

    /**
     * 监听视图解锁开始
     */
    void onStarted();

    /**
     * 图案解锁内容改变
     *
     * @param progress 解锁进度（数字字符串）
     */
    void onProgress(String progress);

    /**
     * 图案解锁完成
     *
     * @param result 解锁结果（数字字符串）
     */
    void onComplete(String result);
}
