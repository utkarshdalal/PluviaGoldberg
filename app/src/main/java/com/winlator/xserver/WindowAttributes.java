package com.winlator.xserver;

import com.winlator.core.Bitmask;
import com.winlator.xconnector.XInputStream;
import java.util.Iterator;

public class WindowAttributes {
    public static final int FLAG_BACKGROUND_PIXMAP = 1<<0;
    public static final int FLAG_BACKGROUND_PIXEL = 1<<1;
    public static final int FLAG_BORDER_PIXMAP = 1<<2;
    public static final int FLAG_BORDER_PIXEL = 1<<3;
    public static final int FLAG_BIT_GRAVITY = 1<<4;
    public static final int FLAG_WIN_GRAVITY = 1<<5;
    public static final int FLAG_BACKING_STORE = 1<<6;
    public static final int FLAG_BACKING_PLANES = 1<<7;
    public static final int FLAG_BACKING_PIXEL = 1<<8;
    public static final int FLAG_OVERRIDE_REDIRECT = 1<<9;
    public static final int FLAG_SAVE_UNDER = 1<<10;
    public static final int FLAG_EVENT_MASK = 1<<11;
    public static final int FLAG_DO_NOT_PROPAGATE_MASK = 1<<12;
    public static final int FLAG_COLORMAP = 1<<13;
    public static final int FLAG_CURSOR = 1<<14;
    public enum BackingStore {NOT_USEFUL, WHEN_MAPPED, ALWAYS}
    public enum WindowClass {COPY_FROM_PARENT, INPUT_OUTPUT, INPUT_ONLY}
    public enum BitGravity {FORGET, NORTH_WEST, NORTH, NORTH_EAST, WEST, CENTER, EAST, SOUTH_WEST, SOUTH, SOUTH_EAST, STATIC}
    public enum WinGravity {UNMAP, NORTH_WEST, NORTH, NORTH_EAST, WEST, CENTER, EAST, SOUTH_WEST, SOUTH, SOUTH_EAST, STATIC}
    private Cursor cursor;
    public final Window window;
    private int backingPixel = 0;
    private int backingPlanes = 1;
    private BackingStore backingStore = BackingStore.NOT_USEFUL;
    private BitGravity bitGravity = BitGravity.CENTER;
    private Bitmask doNotPropagateMask = new Bitmask(0);
    private Bitmask eventMask = new Bitmask(0);
    private WinGravity winGravity = WinGravity.CENTER;
    private WindowClass windowClass = WindowClass.INPUT_OUTPUT;
    private final Bitmask attributeFlags = new Bitmask(new int[]{65536, 262144});

    public WindowAttributes(Window window) {
        this.window = window;
    }

    public int getBackingPixel() {
        return this.backingPixel;
    }

    public int getBackingPlanes() {
        return this.backingPlanes;
    }

    public BackingStore getBackingStore() {
        return this.backingStore;
    }

    public BitGravity getBitGravity() {
        return this.bitGravity;
    }

    public Cursor getCursor() {
        Window parent = this.window.getParent();
        Cursor cursor = this.cursor;
        return (cursor != null || parent == null) ? cursor : parent.attributes.getCursor();
    }

    public Bitmask getEventMask() {
        return this.eventMask;
    }

    public Bitmask getDoNotPropagateMask() {
        return this.doNotPropagateMask;
    }

    public boolean isMapped() {
        return this.attributeFlags.isSet(32768);
    }

    public void setMapped(boolean mapped) {
        this.attributeFlags.set(32768, mapped);
    }

    public boolean isOverrideRedirect() {
        return this.attributeFlags.isSet(512);
    }

    public boolean isSaveUnder() {
        return this.attributeFlags.isSet(1024);
    }

    public WinGravity getWinGravity() {
        return this.winGravity;
    }

    public WindowClass getWindowClass() {
        return this.windowClass;
    }

    public void setWindowClass(WindowClass windowClass) {
        this.windowClass = windowClass;
    }

    public Window getWindow() {
        return window;
    }

    public boolean isEnabled() {
        return this.attributeFlags.isSet(65536);
    }

    public void setEnabled(boolean enabled) {
        this.attributeFlags.set(65536, enabled);
    }

    public boolean isRenderSubwindows() {
        return this.attributeFlags.isSet(262144);
    }

    public void setRenderSubwindows(boolean renderSubwindows) {
        this.attributeFlags.set(262144, renderSubwindows);
    }

    public void update(Bitmask valueMask, XInputStream inputStream, XClient client) {
        for (int index : valueMask) {
            switch (index) {
                case FLAG_BACKGROUND_PIXMAP:
                case FLAG_BORDER_PIXMAP:
                case FLAG_BORDER_PIXEL:
                case FLAG_COLORMAP:
                    inputStream.skip(4);
                    break;
                case FLAG_BACKGROUND_PIXEL:
                    this.window.getContent().fillColor(inputStream.readInt());
                    break;
                case FLAG_BIT_GRAVITY:
                    this.bitGravity = BitGravity.values()[inputStream.readInt()];
                    break;
                case FLAG_WIN_GRAVITY:
                    this.winGravity = WinGravity.values()[inputStream.readInt()];
                    break;
                case FLAG_BACKING_STORE:
                    this.backingStore = BackingStore.values()[inputStream.readInt()];
                    break;
                case FLAG_BACKING_PLANES:
                    this.backingPlanes = inputStream.readInt();
                    break;
                case FLAG_BACKING_PIXEL:
                    this.backingPixel = inputStream.readInt();
                    break;
                case FLAG_OVERRIDE_REDIRECT:
                case FLAG_SAVE_UNDER:
                    this.attributeFlags.set(index, inputStream.readInt() == 1);
                    break;
                case FLAG_EVENT_MASK:
                    this.eventMask = new Bitmask(inputStream.readInt());
                    break;
                case FLAG_DO_NOT_PROPAGATE_MASK:
                    this.doNotPropagateMask = new Bitmask(inputStream.readInt());
                    break;
                case FLAG_CURSOR:
                    this.cursor = client.xServer.cursorManager.getCursor(inputStream.readInt());
                    break;
            }
        }
        client.xServer.windowManager.triggerOnUpdateWindowAttributes(this.window, valueMask);
    }

    public boolean isTransparent() {
        return this.attributeFlags.isSet(131072);
    }

    public void setTransparent(boolean transparent) {
        this.attributeFlags.set(131072, transparent);
    }
}
