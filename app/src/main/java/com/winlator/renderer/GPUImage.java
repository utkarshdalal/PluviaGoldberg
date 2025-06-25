package com.winlator.renderer;

import androidx.annotation.Keep;

import com.winlator.xserver.Drawable;

import java.nio.ByteBuffer;

public class GPUImage extends Texture {
    private long hardwareBufferPtr;
    private long imageKHRPtr;
    private boolean locked;
    private int nativeHandle;
    private ByteBuffer virtualData;
    private short stride;
    private static boolean supported = false;

    static {
        System.loadLibrary("winlator");
    }

    public GPUImage(short width, short height) {
        hardwareBufferPtr = createHardwareBuffer(width, height, true, true);
        if (hardwareBufferPtr != 0) virtualData = lockHardwareBuffer(hardwareBufferPtr);
    }

    public GPUImage(short width, short height, boolean cpuAccess) {
        this(width, height, cpuAccess, true);
    }

    public GPUImage(short width, short height, boolean cpuAccess, boolean useHALPixelFormatBGRA8888) {
        this.locked = false;
        long createHardwareBuffer = createHardwareBuffer(width, height, cpuAccess, useHALPixelFormatBGRA8888);
        this.hardwareBufferPtr = createHardwareBuffer;
        if (cpuAccess && createHardwareBuffer != 0) {
            this.virtualData = lockHardwareBuffer(createHardwareBuffer);
            this.locked = true;
        }
    }

    @Override
    public void allocateTexture(short width, short height, ByteBuffer data) {
        if (isAllocated()) return;
        super.allocateTexture(width, height, null);
        imageKHRPtr = createImageKHR(hardwareBufferPtr, textureId);
    }

    @Override
    public void updateFromDrawable(Drawable drawable) {
        if (!isAllocated()) allocateTexture(drawable.width, drawable.height, null);
        needsUpdate = false;
    }

    public short getStride() {
        return stride;
    }

    @Keep
    private void setStride(short stride) {
        this.stride = stride;
    }

    public int getNativeHandle() {
        return this.nativeHandle;
    }

    @Keep
    private void setNativeHandle(int nativeHandle) {
        this.nativeHandle = nativeHandle;
    }

    public ByteBuffer getVirtualData() {
        return virtualData;
    }

    @Override
    public void destroy() {
        destroyImageKHR(imageKHRPtr);
        destroyHardwareBuffer(hardwareBufferPtr, this.locked);
        virtualData = null;
        imageKHRPtr = 0;
        hardwareBufferPtr = 0;
        super.destroy();
    }

    public static boolean isSupported() {
        return supported;
    }

    public long getHardwareBufferPtr() {
        return this.hardwareBufferPtr;
    }

    public static void checkIsSupported() {
        final short size = 8;
        GPUImage gpuImage = new GPUImage(size, size);
        gpuImage.allocateTexture(size, size, null);
        supported = gpuImage.hardwareBufferPtr != 0 && gpuImage.imageKHRPtr != 0 && gpuImage.virtualData != null;
        gpuImage.destroy();
    }

    private native long createHardwareBuffer(short width, short height, boolean cpuAccess, boolean useHALPixelFormatBGRA8888);

    private native void destroyHardwareBuffer(long hardwareBufferPtr, boolean locked);

    private native ByteBuffer lockHardwareBuffer(long hardwareBufferPtr);

    private native long createImageKHR(long hardwareBufferPtr, int textureId);

    private native void destroyImageKHR(long imageKHRPtr);
}
