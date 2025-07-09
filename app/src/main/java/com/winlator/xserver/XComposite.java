package com.winlator.xserver.extensions;

import com.winlator.xconnector.XInputStream;
import com.winlator.xconnector.XOutputStream;
import com.winlator.xconnector.XStreamLock;
import com.winlator.xserver.Window;
import com.winlator.xserver.XClient;
import com.winlator.xserver.errors.BadAccess;
import com.winlator.xserver.errors.BadImplementation;
import com.winlator.xserver.errors.BadMatch;
import com.winlator.xserver.errors.BadWindow;
import com.winlator.xserver.errors.XRequestError;
import java.io.IOException;

/* loaded from: classes.dex */
public class XComposite implements Extension {

    public static final byte MAJOR_OPCODE = -105;

    public enum UpdateMode {
        REDIRECT_AUTOMATIC,
        REDIRECT_MANUAL
    }

    @Override // com.winlator.xserver.extensions.Extension
    public String getName() {
        return "Composite";
    }

    @Override // com.winlator.xserver.extensions.Extension
    public byte getMajorOpcode() {
        return (byte) -105;
    }

    @Override // com.winlator.xserver.extensions.Extension
    public byte getFirstErrorId() {
        return (byte) 0;
    }

    @Override // com.winlator.xserver.extensions.Extension
    public byte getFirstEventId() {
        return (byte) 0;
    }

    private static void setWindowsToOffscreenStorage(Window window) {
        if (window.attributes.isMapped()) {
            window.getContent().setOffscreenStorage(true);
            for (Window child : window.getChildren()) {
                setWindowsToOffscreenStorage(child);
            }
        }
    }

    private static void queryVersion(XClient client, XInputStream inputStream, XOutputStream outputStream) throws XRequestError, IOException {
        inputStream.skip(8);
        XStreamLock lock = outputStream.lock();
        try {
            outputStream.writeByte((byte) 1);
            outputStream.writeByte((byte) 0);
            outputStream.writeShort(client.getSequenceNumber());
            outputStream.writeInt(0);
            outputStream.writeInt(0);
            outputStream.writeInt(1);
            outputStream.writePad(16);
            if (lock != null) {
                lock.close();
            }
        } catch (Throwable th) {
            if (lock != null) {
                try {
                    lock.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private static void redirectWindow(XClient client, XInputStream inputStream, XOutputStream outputStream) throws XRequestError, IOException {
        int windowId = inputStream.readInt();
        byte updateMode = inputStream.readByte();
        inputStream.skip(3);
        Window window = client.xServer.windowManager.getWindow(windowId);
        if (window == null) {
            throw new BadWindow(windowId);
        }
        if (window == client.xServer.windowManager.rootWindow) {
            throw new BadMatch();
        }
        if (updateMode != UpdateMode.REDIRECT_MANUAL.ordinal()) {
            throw new BadImplementation();
        }
        if (((Boolean) window.getTag("compositeRedirectManual", false)).booleanValue()) {
            throw new BadAccess();
        }
        window.setTag("compositeRedirectManual", true);
        setWindowsToOffscreenStorage(window);
        window.getParent().attributes.setRenderSubwindows(false);
    }

    @Override // com.winlator.xserver.extensions.Extension
    public void handleRequest(XClient client, XInputStream inputStream, XOutputStream outputStream) throws XRequestError, IOException {
        int opcode = client.getRequestData();
        switch (opcode) {
            case 0:
                queryVersion(client, inputStream, outputStream);
                return;
            case 1:
                redirectWindow(client, inputStream, outputStream);
                return;
            default:
                throw new BadImplementation();
        }
    }
}
