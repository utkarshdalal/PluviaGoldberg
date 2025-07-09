package com.winlator.xconnector;

import com.winlator.xserver.XServer;
import dalvik.annotation.optimization.CriticalNative;
import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public class XInputStream {
    private final long nativePtr;

    @CriticalNative
    private static native int available(long j);

    private static native void destroy(long j);

    @CriticalNative
    private static native int getActivePosition(long j);

    @CriticalNative
    private static native int getAncillaryFd(long j);

    private native long nativeAllocate(int i, int i2);

    @CriticalNative
    private static native byte readByte(long j);

    private static native ByteBuffer readByteBuffer(long j, int i);

    @CriticalNative
    private static native int readInt(long j);

    private static native int readMoreData(long j, boolean z);

    @CriticalNative
    private static native short readShort(long j);

    @CriticalNative
    private static native void setActivePosition(long j, int i);

    @CriticalNative
    private static native void skip(long j, int i);

    static {
        System.loadLibrary("winlator");
    }

    public XInputStream(int clientFd, int initialCapacity) {
        this.nativePtr = nativeAllocate(clientFd, initialCapacity);
    }

    public int readMoreData(boolean canReceiveAncillaryMessages) {
        return readMoreData(this.nativePtr, canReceiveAncillaryMessages);
    }

    public int getAncillaryFd() {
        return getAncillaryFd(this.nativePtr);
    }

    public int getActivePosition() {
        return getActivePosition(this.nativePtr);
    }

    public void setActivePosition(int activePosition) {
        setActivePosition(this.nativePtr, activePosition);
    }

    public int available() {
        return available(this.nativePtr);
    }

    public byte readByte() {
        return readByte(this.nativePtr);
    }

    public int readUnsignedByte() {
        return Byte.toUnsignedInt(readByte(this.nativePtr));
    }

    public short readShort() {
        return readShort(this.nativePtr);
    }

    public int readUnsignedShort() {
        return Short.toUnsignedInt(readShort(this.nativePtr));
    }

    public int readInt() {
        return readInt(this.nativePtr);
    }

    public long readUnsignedInt() {
        return Integer.toUnsignedLong(readInt(this.nativePtr));
    }

    public void read(byte[] result) {
        for (int i = 0; i < result.length; i++) {
            result[i] = readByte();
        }
    }

    public ByteBuffer readByteBuffer(int length) {
        return readByteBuffer(this.nativePtr, length);
    }

    public String readString8(int length) {
        byte[] bytes = new byte[length];
        read(bytes);
        String str = new String(bytes, XServer.LATIN1_CHARSET);
        if (((-length) & 3) > 0) {
            skip((-length) & 3);
        }
        return str;
    }

    public void skip(int length) {
        skip(this.nativePtr, length);
    }

    public void destroy() {
        destroy(this.nativePtr);
    }
}
