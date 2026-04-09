package edu.hitsz.application;

/**
 * 应用全局设置类，存储屏幕尺寸等运行时参数
 */
public class AppSettings {
    public static int WINDOW_WIDTH = 480;  // 默认值，稍后会被实际屏幕宽高覆盖
    public static int WINDOW_HEIGHT = 800; // 默认值

    /**
     * 由 GameView 在 surfaceChanged 中调用，更新实际屏幕尺寸
     */
    public static void setScreenSize(int width, int height) {
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
    }
}