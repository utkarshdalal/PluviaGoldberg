package com.winlator.xserver;

import android.graphics.Bitmap;

import com.winlator.core.Callback;
import com.winlator.math.Mathf;
import com.winlator.renderer.GPUImage;
import com.winlator.renderer.Texture;
import com.winlator.xserver.GraphicsContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Drawable extends XResource {
    private ByteBuffer data;
    public final short height;
    private boolean offscreenStorage;
    private Callback<Drawable> onDestroyListener;
    private Runnable onDrawListener;
    public final Object renderLock;
    private Texture texture;
    private boolean useSharedData;
    public final Visual visual;
    public final short width;

    private static native void copyArea(short s, short s2, short s3, short s4, short s5, short s6, short s7, short s8, ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    private static native void copyAreaOp(short s, short s2, short s3, short s4, short s5, short s6, short s7, short s8, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i);

    private static native void drawAlphaMaskedBitmap(byte b, byte b2, byte b3, byte b4, byte b5, byte b6, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3);

    private static native void drawBitmap(short s, short s2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    private static native void drawLine(short s, short s2, short s3, short s4, int i, short s5, short s6, ByteBuffer byteBuffer);

    private static native void fillRect(short s, short s2, short s3, short s4, int i, short s5, ByteBuffer byteBuffer);

    private static native void fromBitmap(Bitmap bitmap, ByteBuffer byteBuffer);

    static {
        System.loadLibrary("winlator");
    }

    public Drawable(int id, int width, int height, Visual visual) {
        super(id);
        this.texture = new Texture();
        this.offscreenStorage = false;
        this.renderLock = new Object();
        this.width = (short)width;
        this.height = (short)height;
        this.visual = visual;
        this.data = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static Drawable fromBitmap(Bitmap bitmap) {
        Drawable drawable = new Drawable(0, bitmap.getWidth(), bitmap.getHeight(), null);
        fromBitmap(bitmap, drawable.data);
        return drawable;
    }

    public boolean isOffscreenStorage() {
        return this.offscreenStorage;
    }

    public void setOffscreenStorage(boolean offscreenStorage) {
        this.offscreenStorage = offscreenStorage;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(Texture texture) {
        if (texture instanceof GPUImage) {
            this.data = ((GPUImage) texture).getVirtualData();
        }
        this.texture = texture;
    }

    public ByteBuffer getData() {
        return this.data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    private short getStride() {
        Texture texture = this.texture;
        return texture instanceof GPUImage ? ((GPUImage) texture).getStride() : this.width;
    }

    public Runnable getOnDrawListener() {
        return this.onDrawListener;
    }

    public void setOnDrawListener(Runnable onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public Callback<Drawable> getOnDestroyListener() {
        return this.onDestroyListener;
    }

    public void setOnDestroyListener(Callback<Drawable> onDestroyListener) {
        this.onDestroyListener = onDestroyListener;
    }

    public void drawImage(short srcX, short srcY, short dstX, short dstY, short width, short height, byte depth, ByteBuffer data, short totalWidth, short totalHeight) {
        short width2 = width;
        short height2 = height;
        ByteBuffer byteBuffer = this.data;
        if (byteBuffer == null) {
            return;
        }
        if (depth == 1) {
            drawBitmap(width2, height2, data, byteBuffer);
        } else {
            if (depth == 24 || depth == 32) {
                short dstX2 = (short) Mathf.clamp((int) dstX, 0, this.width - 1);
                short dstY2 = (short) Mathf.clamp((int) dstY, 0, this.height - 1);
                int i = dstX2 + width2;
                short s = this.width;
                if (i > s) {
                    width2 = (short) (s - dstX2);
                }
                int i2 = dstY2 + height2;
                short s2 = this.height;
                if (i2 > s2) {
                    height2 = (short) (s2 - dstY2);
        }
                copyArea(srcX, srcY, dstX2, dstY2, width2, height2, totalWidth, getStride(), data, this.data);
        }
            this.data.rewind();
            data.rewind();
            forceUpdate();
        }
        this.data.rewind();
        data.rewind();
        forceUpdate();
    }

    public ByteBuffer getImage(short x, short y, short width, short height) {
        ByteBuffer dstData = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
        if (this.data == null) {
            return dstData;
        }
        short x2 = (short) Mathf.clamp((int) x, 0, this.width - 1);
        short y2 = (short) Mathf.clamp((int) y, 0, this.height - 1);
        int i = x2 + width;
        short s = this.width;
        short width2 = i > s ? (short) (s - x2) : width;
        int i2 = y2 + height;
        short s2 = this.height;
        copyArea(x2, y2, (short) 0, (short) 0, width2, i2 > s2 ? (short) (s2 - y2) : height, getStride(), width2, this.data, dstData);
        this.data.rewind();
        dstData.rewind();
        return dstData;
    }

    public void copyArea(short srcX, short srcY, short dstX, short dstY, short width, short height, Drawable drawable) {
        copyArea(srcX, srcY, dstX, dstY, width, height, drawable, GraphicsContext.Function.COPY);
    }

    public void copyArea(short srcX, short srcY, short dstX, short dstY, short width, short height, Drawable drawable, GraphicsContext.Function gcFunction) {
        if (this.data != null && drawable.data != null) {
            short dstX2 = (short) Mathf.clamp((int) dstX, 0, this.width - 1);
            short dstY2 = (short) Mathf.clamp((int) dstY, 0, this.height - 1);
            int i = dstX2 + width;
            short s = this.width;
            short width2 = i > s ? (short) (s - dstX2) : width;
            int i2 = dstY2 + height;
            short s2 = this.height;
            short height2 = i2 > s2 ? (short) (s2 - dstY2) : height;
        if (gcFunction == GraphicsContext.Function.COPY) {
                copyArea(srcX, srcY, dstX2, dstY2, width2, height2, drawable.getStride(), getStride(), drawable.data, this.data);
            } else {
                copyAreaOp(srcX, srcY, dstX2, dstY2, width2, height2, drawable.getStride(), getStride(), drawable.data, this.data, gcFunction.ordinal());
        }
        this.data.rewind();
        drawable.data.rewind();
            forceUpdate();
        }
    }

    public void fillColor(int color) {
        fillRect(0, 0, this.width, this.height, color);
    }

    public void fillRect(int x, int y, int width, int height, int color) {
        if (this.data == null) {
            return;
        }
        int x2 = (short) Mathf.clamp(x, 0, this.width - 1);
        int y2 = (short) Mathf.clamp(y, 0, this.height - 1);
        int i = x2 + width;
        int i2 = this.width;
        if (i > i2) {
            width = (short) (i2 - x2);
        }
        int i3 = y2 + height;
        int i4 = this.height;
        if (i3 > i4) {
            height = (short) (i4 - y2);
        }
        fillRect((short) x2, (short) y2, (short) width, (short) height, color, getStride(), this.data);
        this.data.rewind();
        forceUpdate();
    }

    public void drawLines(int color, int lineWidth, short... points) {
        for (int i = 2; i < points.length; i += 2) {
            drawLine(points[i-2], points[i-1], points[i+0], points[i+1], color, (short)lineWidth);
        }
    }

    public void drawLine(int x0, int y0, int x1, int y1, int color, int lineWidth) {
        if (this.data == null) {
            return;
        }
        drawLine((short) Mathf.clamp(x0, 0, this.width - lineWidth), (short) Mathf.clamp(y0, 0, this.height - lineWidth), (short) Mathf.clamp(x1, 0, this.width - lineWidth), (short) Mathf.clamp(y1, 0, this.height - lineWidth), color, (short) lineWidth, getStride(), this.data);
        this.data.rewind();
        forceUpdate();
    }

    public void drawAlphaMaskedBitmap(byte foreRed, byte foreGreen, byte foreBlue, byte backRed, byte backGreen, byte backBlue, Drawable srcDrawable, Drawable maskDrawable) {
        ByteBuffer byteBuffer;
        ByteBuffer byteBuffer2 = this.data;
        if (byteBuffer2 != null && (byteBuffer = srcDrawable.data) != null) {
            ByteBuffer byteBuffer3 = maskDrawable.data;
            if (byteBuffer3 == null) {
                return;
            }
            drawAlphaMaskedBitmap(foreRed, foreGreen, foreBlue, backRed, backGreen, backBlue, byteBuffer, byteBuffer3, byteBuffer2);
        this.data.rewind();
            forceUpdate();
        }
    }

    public void forceUpdate() {
        if (!this.offscreenStorage) {
            this.texture.setNeedsUpdate(true);
            Runnable runnable = this.onDrawListener;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public boolean isUseSharedData() {
        return this.useSharedData;
    }

    public void setUseSharedData(boolean useSharedData) {
        this.useSharedData = useSharedData;
    }
}
