package com.winlator.xconnector;

import com.winlator.xserver.XServer;
import dalvik.annotation.optimization.CriticalNative;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/* loaded from: classes.dex */
public class XOutputStream {
    private final ReentrantLock lock = new ReentrantLock();
    private final long nativePtr;

    private static native void destroy(long j);

    private native long nativeAllocate(int i, int i2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native boolean sendData(long j);

    @CriticalNative
    private static native void setAncillaryFd(long j, int i);

    @CriticalNative
    private static native void writeByte(long j, byte b);

    private static native void writeByteBuffer(long j, ByteBuffer byteBuffer, int i, int i2);

    @CriticalNative
    private static native void writeInt(long j, int i);

    @CriticalNative
    private static native void writeLong(long j, long j2);

    @CriticalNative
    private static native void writePad(long j, int i);

    @CriticalNative
    private static native void writeShort(long j, short s);

    static {
        System.loadLibrary("winlator");
    }

    public XOutputStream(int clientFd, int initialCapacity) {
        this.nativePtr = nativeAllocate(clientFd, initialCapacity);
    }

    public void setAncillaryFd(int ancillaryFd) {
        setAncillaryFd(this.nativePtr, ancillaryFd);
    }

    public void writeByte(byte value) {
        writeByte(this.nativePtr, value);
    }

    public void writeShort(short value) {
        writeShort(this.nativePtr, value);
    }

    public void writeInt(int value) {
        writeInt(this.nativePtr, value);
    }

    public void writeLong(long value) {
        writeLong(this.nativePtr, value);
    }

    public void writeString8(String str) {
        byte[] bytes = str.getBytes(XServer.LATIN1_CHARSET);
        int length = (-str.length()) & 3;
        write(bytes);
        if (length > 0) {
            writePad(length);
        }
    }

    public void write(byte[] data) {
        write(data, 0, data.length);
    }

    public void write(byte[] data, int offset, int length) {
        for (int i = offset; i < length; i++) {
            writeByte(this.nativePtr, data[i]);
        }
    }

    public void write(ByteBuffer data) {
        if (data.isDirect()) {
            writeByteBuffer(this.nativePtr, data, data.position(), data.remaining());
            return;
        }
        int length = data.remaining();
        for (int i = data.position(); i < length; i++) {
            writeByte(this.nativePtr, data.get(i));
        }
    }

    public void writePad(int length) {
        writePad(this.nativePtr, length);
    }

    public XStreamLock lock() {
        return new OutputStreamLock();
    }

    public void destroy() {
        destroy(this.nativePtr);
    }

    private class OutputStreamLock implements XStreamLock {
        public OutputStreamLock() {
            XOutputStream.this.lock.lock();
        }

        @Override // com.winlator.xconnector.XStreamLock, java.lang.AutoCloseable
        public void close() throws IOException {
            try {
                if (!XOutputStream.sendData(XOutputStream.this.nativePtr)) {
                    throw new IOException("Failed to send data.");
                }
            } finally {
                XOutputStream.this.lock.unlock();
            }
        }
    }
}
