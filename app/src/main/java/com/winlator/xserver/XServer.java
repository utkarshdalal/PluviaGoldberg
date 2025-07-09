package com.winlator.xserver;

import android.util.Log;
import android.util.SparseArray;

import com.winlator.core.CursorLocker;
import com.winlator.renderer.GLRenderer;
import com.winlator.winhandler.WinHandler;
import com.winlator.xserver.Pointer;
import com.winlator.xserver.extensions.BigReqExtension;
import com.winlator.xserver.extensions.DRI3Extension;
import com.winlator.xserver.extensions.Extension;
import com.winlator.xserver.extensions.MITSHMExtension;
import com.winlator.xserver.extensions.PresentExtension;
import com.winlator.xserver.extensions.SyncExtension;
import com.winlator.xserver.extensions.XComposite;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.concurrent.locks.ReentrantLock;

public class XServer {
    public static final short VERSION = 11;
    public static final String VENDOR_NAME = "Elbrus Technologies, LLC";
    public static final Charset LATIN1_CHARSET = Charset.forName("latin1");
    public final CursorManager cursorManager;
    public final DrawableManager drawableManager;
    public final GrabManager grabManager;
    public final InputDeviceManager inputDeviceManager;
    public final PixmapManager pixmapManager;
    private GLRenderer renderer;
    public final ScreenInfo screenInfo;
    public final SelectionManager selectionManager;
    private SHMSegmentManager shmSegmentManager;
    private WinHandler winHandler;
    public final WindowManager windowManager;
    public final SparseArray<Extension> extensions = new SparseArray<>();
    public final ResourceIDs resourceIDs = new ResourceIDs(128);
    public final GraphicsContextManager graphicsContextManager = new GraphicsContextManager();
    public final Keyboard keyboard = Keyboard.createKeyboard(this);
    public final Pointer pointer = new Pointer(this);
    private final EnumMap<Lockable, ReentrantLock> locks = new EnumMap<>(Lockable.class);
    private boolean relativeMouseMovement = false;
    public final CursorLocker cursorLocker;

    public enum Lockable {
        WINDOW_MANAGER,
        PIXMAP_MANAGER,
        DRAWABLE_MANAGER,
        GRAPHIC_CONTEXT_MANAGER,
        INPUT_DEVICE,
        CURSOR_MANAGER,
        SHMSEGMENT_MANAGER
    }

    public XServer(ScreenInfo screenInfo) {
        Log.d("XServer", "Creating xServer " + screenInfo);
        this.screenInfo = screenInfo;
        cursorLocker = new CursorLocker(this);
        for (Lockable lockable : Lockable.values()) {
            locks.put(lockable, new ReentrantLock());
        }
        this.pixmapManager = new PixmapManager();
        DrawableManager drawableManager = new DrawableManager(this);
        this.drawableManager = drawableManager;
        this.cursorManager = new CursorManager(drawableManager);
        WindowManager windowManager = new WindowManager(screenInfo, drawableManager);
        this.windowManager = windowManager;
        this.selectionManager = new SelectionManager(windowManager);
        this.inputDeviceManager = new InputDeviceManager(this);
        this.grabManager = new GrabManager(this);
        DesktopHelper.attachTo(this);
        setupExtensions();
    }

    public boolean isRelativeMouseMovement() {
        return relativeMouseMovement;
    }

    public void setRelativeMouseMovement(boolean relativeMouseMovement) {
        cursorLocker.setEnabled(!relativeMouseMovement);
        this.relativeMouseMovement = relativeMouseMovement;
    }

    public GLRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(GLRenderer renderer) {
        this.renderer = renderer;
    }

    public WinHandler getWinHandler() {
        return winHandler;
    }

    public void setWinHandler(WinHandler winHandler) {
        this.winHandler = winHandler;
    }

    public SHMSegmentManager getSHMSegmentManager() {
        return shmSegmentManager;
    }

    public void setSHMSegmentManager(SHMSegmentManager shmSegmentManager) {
        this.shmSegmentManager = shmSegmentManager;
    }

    private class SingleXLock implements XLock {
        private final ReentrantLock lock;

        private SingleXLock(Lockable lockable) {
            ReentrantLock reentrantLock = (ReentrantLock) XServer.this.locks.get(lockable);
            this.lock = reentrantLock;
            reentrantLock.lock();
        }

        @Override
        public void close() {
            this.lock.unlock();
        }
    }

    private class MultiXLock implements XLock {
        private final Lockable[] lockables;

        private MultiXLock(Lockable[] lockables) {
            this.lockables = lockables;
            for (Lockable lockable : lockables) {
                ((ReentrantLock) XServer.this.locks.get(lockable)).lock();
            }
        }

        @Override
        public void close() {
            for (int i = this.lockables.length - 1; i >= 0; i--) {
                ((ReentrantLock) XServer.this.locks.get(this.lockables[i])).unlock();
            }
        }
    }

    public XLock lock(Lockable lockable) {
        return new SingleXLock(lockable);
    }

    public XLock lock(Lockable... lockables) {
        return new MultiXLock(lockables);
    }

    public XLock lockAll() {
        return new MultiXLock(Lockable.values());
    }

    public Extension getExtensionByName(String name) {
        for (int i = 0; i < this.extensions.size(); i++) {
            Extension extension = this.extensions.valueAt(i);
            if (extension.getName().equals(name)) {
                return extension;
            }
        }
        return null;
    }

    public void injectPointerMove(int x, int y) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            pointer.setPosition(x, y);
        }
    }

    public void injectPointerMoveDelta(int dx, int dy) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            pointer.setPosition(pointer.getX() + dx, pointer.getY() + dy);
        }
    }

    public void injectPointerButtonPress(Pointer.Button buttonCode) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            pointer.setButton(buttonCode, true);
        }
    }

    public void injectPointerButtonRelease(Pointer.Button buttonCode) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            pointer.setButton(buttonCode, false);
        }
    }

    public void injectKeyPress(XKeycode xKeycode) {
        injectKeyPress(xKeycode, 0);
    }

    public void injectKeyPress(XKeycode xKeycode, int keysym) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            keyboard.setKeyPress(xKeycode.getId(), keysym);
        }
    }

    public void injectKeyRelease(XKeycode xKeycode) {
        try (XLock lock = lock(Lockable.WINDOW_MANAGER, Lockable.INPUT_DEVICE)) {
            keyboard.setKeyRelease(xKeycode.getId());
        }
    }

    private void setupExtensions() {
        extensions.put(BigReqExtension.MAJOR_OPCODE, new BigReqExtension());
        extensions.put(MITSHMExtension.MAJOR_OPCODE, new MITSHMExtension());
        extensions.put(DRI3Extension.MAJOR_OPCODE, new DRI3Extension());
        extensions.put(PresentExtension.MAJOR_OPCODE, new PresentExtension());
        extensions.put(SyncExtension.MAJOR_OPCODE, new SyncExtension());
        extensions.put(XComposite.MAJOR_OPCODE, new XComposite());
    }

    public <T extends Extension> T getExtension(int opcode) {
        return (T)extensions.get(opcode);
    }
}
