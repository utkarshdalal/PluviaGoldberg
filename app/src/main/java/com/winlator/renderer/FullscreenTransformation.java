package com.winlator.renderer;

import com.winlator.xserver.ScreenInfo;
import com.winlator.xserver.Window;

public class FullscreenTransformation {
    public short height;
    public short width;
    private final Window window;
    public short x;
    public short y;

    public FullscreenTransformation(Window window) {
        this.window = window;
    }

    public void update(ScreenInfo screenInfo, short originWidth, short originHeight) {
        short targetHeight = (short) Math.min(screenInfo.height, (screenInfo.width / originWidth) * originHeight);
        short targetWidth = (short) ((targetHeight / originHeight) * originWidth);
        this.x = (short) ((screenInfo.width - targetWidth) * 0.5f);
        this.y = (short) ((screenInfo.height - targetHeight) * 0.5f);
        this.width = targetWidth;
        this.height = targetHeight;
    }

    public short[] transformPointerCoords(short x, short y) {
        short[] localPoint = this.window.rootPointToLocal(x, y, true);
        float scaleX = this.window.getWidth() / this.width;
        float scaleY = this.window.getHeight() / this.height;
        short transformedX = (short) Math.max(0.0f, (localPoint[0] * scaleX) + this.window.getRootX());
        short transformedY = (short) Math.max(0.0f, (localPoint[1] * scaleY) + this.window.getRootY());
        return new short[]{transformedX, transformedY};
    }
}
