package com.winlator.sysvshm;

import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.RequestHandler;
import com.winlator.xconnector.XInputStream;
import com.winlator.xconnector.XOutputStream;
import com.winlator.xconnector.XStreamLock;

import java.io.IOException;

public class SysVSHMRequestHandler implements RequestHandler {
    @Override
    public boolean handleRequest(ConnectedClient client) throws IOException {
        XStreamLock lock;
        SysVSharedMemory sysVSharedMemory = (SysVSharedMemory)client.getTag();
        XInputStream inputStream = client.getInputStream();
        XOutputStream outputStream = client.getOutputStream();

        if (inputStream.available() < 5) return false;
        byte requestCode = inputStream.readByte();

        switch (requestCode) {
            case RequestCodes.SHMGET:
                long size = inputStream.readUnsignedInt();
                int shmid = sysVSharedMemory.get(size);
                lock = outputStream.lock();
                try {
                    outputStream.writeInt(shmid);
                    if (lock != null) {
                        lock.close();
                        return true;
                    }
                    return true;
                } finally {
                }
            case RequestCodes.GET_FD:
                int shmid2 = inputStream.readInt();
                lock = outputStream.lock();
                try {
                    outputStream.writeByte((byte)0);
                    outputStream.setAncillaryFd(sysVSharedMemory.getFd(shmid2));
                    if (lock != null) {
                        lock.close();
                        return true;
                    }
                    return true;
                } finally {
                }
            case RequestCodes.DELETE:
                int shmid3 = inputStream.readInt();
                sysVSharedMemory.delete(shmid3);
                return true;
            default:
                return true;
        }
    }
}
