package com.winlator.xconnector;

import com.winlator.xserver.XServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class XInputStream {
    private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024; // 16MB maximum buffer size
    private ByteBuffer activeBuffer;
    private ByteBuffer buffer;
    public final ClientSocket clientSocket;

    public XInputStream(int initialCapacity) {
        this(null, initialCapacity);
    }

    public XInputStream(ClientSocket clientSocket, int initialCapacity) {
        this.clientSocket = clientSocket;
        this.buffer = ByteBuffer.allocateDirect(initialCapacity);
    }

    public int readMoreData(boolean canReceiveAncillaryMessages) throws IOException {
        // If there's an active buffer, check for remaining data or compact if necessary
        if (activeBuffer != null) {
            if (!activeBuffer.hasRemaining()) {
                buffer.clear();
            } else if (activeBuffer.position() > 0) {
                compactBuffer();
            }
            activeBuffer = null;
        }

        // Grow the buffer if necessary
        growInputBufferIfNecessary();

        // Read data from the socket
        int bytesRead = canReceiveAncillaryMessages ? clientSocket.recvAncillaryMsg(buffer) : clientSocket.read(buffer);

        // If data is read, prepare the buffer for reading
        if (bytesRead > 0) {
            prepareActiveBuffer();
        }
        return bytesRead;
    }

    public int getAncillaryFd() {
        return clientSocket.getAncillaryFd();
    }

    private void growInputBufferIfNecessary() {
        // Only grow the buffer if it's full and hasn't exceeded the max size
        if (buffer.position() == buffer.capacity() && buffer.capacity() < MAX_BUFFER_SIZE) {
            int newCapacity = Math.min(buffer.capacity() * 2, MAX_BUFFER_SIZE);
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity).order(buffer.order());
            buffer.flip(); // Prepare the buffer for reading
            newBuffer.put(buffer); // Copy data to the new buffer
            buffer = newBuffer;
        }
    }

    private void compactBuffer() {
        int newLimit = buffer.position();
        buffer.position(activeBuffer.position()).limit(newLimit);
        buffer.compact();
    }

    private void prepareActiveBuffer() {
        int position = buffer.position();
        buffer.flip();
        activeBuffer = buffer.slice().order(buffer.order());
        buffer.limit(buffer.capacity()).position(position);
    }

    public void setByteOrder(ByteOrder byteOrder) {
        buffer.order(byteOrder);
        if (activeBuffer != null) activeBuffer.order(byteOrder);
    }

    public int getActivePosition() {
        return activeBuffer != null ? activeBuffer.position() : 0;
    }

    public void setActivePosition(int activePosition) {
        if (activeBuffer != null) activeBuffer.position(activePosition);
    }

    public int available() {
        return activeBuffer != null ? activeBuffer.remaining() : 0;
    }

    public byte readByte() {
        return activeBuffer.get();
    }

    public int readUnsignedByte() {
        return Byte.toUnsignedInt(activeBuffer.get());
    }

    public short readShort() {
        return activeBuffer.getShort();
    }

    public int readUnsignedShort() {
        return Short.toUnsignedInt(activeBuffer.getShort());
    }

    public int readInt() {
        return activeBuffer.getInt();
    }

    public long readUnsignedInt() {
        return Integer.toUnsignedLong(activeBuffer.getInt());
    }

    public long readLong() {
        return activeBuffer.getLong();
    }

    public void read(byte[] result) {
        activeBuffer.get(result);
    }

    public ByteBuffer readByteBuffer(int length) {
        ByteBuffer newBuffer = activeBuffer.slice().order(activeBuffer.order());
        newBuffer.limit(length);
        activeBuffer.position(activeBuffer.position() + length);
        return newBuffer;
    }

    public String readString8(int length) {
        byte[] bytes = new byte[length];
        read(bytes);
        String str = new String(bytes, XServer.LATIN1_CHARSET);
        if ((-length & 3) > 0) skip(-length & 3);
        return str;
    }

    public void skip(int length) {
        activeBuffer.position(activeBuffer.position() + length);
    }
}
