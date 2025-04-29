package com.winlator;

import android.os.Build;

/*
    WinlatorXR implementation by lvonasek (https://github.com/lvonasek)
 */

public class XrActivity {
    // Order of the enum has to be the as in xr/main.cpp
    public enum ControllerAxis {
        L_PITCH, L_YAW, L_ROLL, L_THUMBSTICK_X, L_THUMBSTICK_Y, L_X, L_Y, L_Z,
        R_PITCH, R_YAW, R_ROLL, R_THUMBSTICK_X, R_THUMBSTICK_Y, R_X, R_Y, R_Z,
        HMD_PITCH, HMD_YAW, HMD_ROLL, HMD_X, HMD_Y, HMD_Z, HMD_IPD
    }

    // Order of the enum has to be the as in xr/main.cpp
    public enum ControllerButton {
        L_GRIP,  L_MENU, L_THUMBSTICK_PRESS, L_THUMBSTICK_LEFT, L_THUMBSTICK_RIGHT, L_THUMBSTICK_UP, L_THUMBSTICK_DOWN, L_TRIGGER, L_X, L_Y,
        R_A, R_B, R_GRIP, R_THUMBSTICK_PRESS, R_THUMBSTICK_LEFT, R_THUMBSTICK_RIGHT, R_THUMBSTICK_UP, R_THUMBSTICK_DOWN, R_TRIGGER,
    }

    private static boolean isDeviceDetectionFinished = false;
    private static boolean isDeviceSupported = false;
    private static boolean isImmersive = false;
    private static boolean isSBS = false;
    private static final float[] lastAxes = new float[ControllerAxis.values().length];
    private static final boolean[] lastButtons = new boolean[ControllerButton.values().length];
    private static String lastText = "";
    private static float mouseSpeed = 1;
    private static final float[] smoothedMouse = new float[2];
    private static XrActivity instance;

    public static XrActivity getInstance() {
        return instance;
    }

    public static boolean getImmersive() {
        return isImmersive;
    }

    public static boolean getSBS() {
        return isSBS;
    }

    public static boolean isSupported() {
        if (!isDeviceDetectionFinished) {
            if (Build.MANUFACTURER.compareToIgnoreCase("META") == 0) {
                isDeviceSupported = true;
            }
            if (Build.MANUFACTURER.compareToIgnoreCase("OCULUS") == 0) {
                isDeviceSupported = true;
            }
            isDeviceDetectionFinished = true;
        }
        return isDeviceSupported;
    }

    // Rendering
    public native void init();
    public native void bindFramebuffer();
    public native int getWidth();
    public native int getHeight();
    public native boolean beginFrame(boolean immersive, boolean sbs);
    public native void endFrame();

    // Input
    public native float[] getAxes();
    public native boolean[] getButtons();
}
