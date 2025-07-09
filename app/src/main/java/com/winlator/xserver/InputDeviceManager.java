package com.winlator.xserver;

import com.winlator.core.Bitmask;
import com.winlator.renderer.FullscreenTransformation;
import com.winlator.winhandler.MouseEventFlags;
import com.winlator.winhandler.WinHandler;
import com.winlator.xserver.Keyboard;
import com.winlator.xserver.Pointer;
import com.winlator.xserver.WindowManager;
import com.winlator.xserver.XResourceManager;
import com.winlator.xserver.events.ButtonPress;
import com.winlator.xserver.events.ButtonRelease;
import com.winlator.xserver.events.EnterNotify;
import com.winlator.xserver.events.Event;
import com.winlator.xserver.events.KeyPress;
import com.winlator.xserver.events.KeyRelease;
import com.winlator.xserver.events.LeaveNotify;
import com.winlator.xserver.events.MappingNotify;
import com.winlator.xserver.events.MotionNotify;
import com.winlator.xserver.events.PointerWindowEvent;

public class InputDeviceManager implements Pointer.OnPointerMotionListener, Keyboard.OnKeyboardListener, WindowManager.OnWindowModificationListener, XResourceManager.OnResourceLifecycleListener {
    private static final byte MOUSE_WHEEL_DELTA = 120;
    private Window pointWindow;
    private final XServer xServer;

    public InputDeviceManager(XServer xServer) {
        this.xServer = xServer;
        WindowManager windowManager = xServer.windowManager;
        this.pointWindow = windowManager.rootWindow;
        windowManager.addOnWindowModificationListener(this);
        xServer.windowManager.addOnResourceLifecycleListener(this);
        xServer.pointer.addOnPointerMotionListener(this);
        xServer.keyboard.addOnKeyboardListener(this);
    }

    @Override
    public void onMapWindow(Window window) {
        updatePointWindow();
    }

    @Override
    public void onUnmapWindow(Window window) {
        updatePointWindow();
    }

    @Override
    public void onChangeWindowZOrder(Window window) {
        updatePointWindow();
    }

    @Override
    public void onUpdateWindowGeometry(Window window, boolean resized) {
        updatePointWindow();
    }

    @Override
    public void onCreateResource(XResource resource) {
        updatePointWindow();
    }

    @Override
    public void onFreeResource(XResource resource) {
        updatePointWindow();
    }

    private void updatePointWindow() {
        XServer xServer = this.xServer;
        Window pointWindow = xServer.windowManager.findPointWindow(xServer.pointer.getClampedX(), this.xServer.pointer.getClampedY(), true);
        this.pointWindow = pointWindow != null ? pointWindow : this.xServer.windowManager.rootWindow;
    }

    public Window getPointWindow() {
        return this.pointWindow;
    }

    private void sendEvent(Window window, int eventId, Event event) {
        Window grabWindow = this.xServer.grabManager.getWindow();
        if (grabWindow != null && grabWindow.attributes.isEnabled()) {
            EventListener eventListener = this.xServer.grabManager.getEventListener();
            if (this.xServer.grabManager.isOwnerEvents() && window != null) {
                window.sendEvent(eventId, event, this.xServer.grabManager.getClient());
                return;
            } else {
                if (eventListener.isInterestedIn(eventId)) {
                eventListener.sendEvent(event);
                    return;
                }
                return;
            }
        }
        if (window != null && window.attributes.isEnabled()) {
            window.sendEvent(eventId, event);
        }
    }

    private void sendEvent(Window window, Bitmask eventMask, Event event) {
        Window grabWindow = this.xServer.grabManager.getWindow();
        if (grabWindow != null && grabWindow.attributes.isEnabled()) {
            EventListener eventListener = this.xServer.grabManager.getEventListener();
            if (this.xServer.grabManager.isOwnerEvents() && window != null) {
                window.sendEvent(eventMask, event, eventListener.client);
                return;
            } else {
                if (eventListener.isInterestedIn(eventMask)) {
                eventListener.sendEvent(event);
                    return;
                }
                return;
            }
        }
        if (window != null && window.attributes.isEnabled()) {
            window.sendEvent(eventMask, event);
        }
    }

    public void sendEnterLeaveNotify(Window windowA, Window windowB, PointerWindowEvent.Mode mode) {
        PointerWindowEvent.Detail detailA;
        PointerWindowEvent.Detail detailB;
        short xA;
        short yA;
        short xB;
        short yB;
        if (windowA == windowB) {
            return;
        }
        boolean sameScreenAndFocus = windowB.isAncestorOf(this.xServer.windowManager.getFocusedWindow());
        PointerWindowEvent.Detail detailA2 = PointerWindowEvent.Detail.NONLINEAR;
        PointerWindowEvent.Detail detailB2 = PointerWindowEvent.Detail.NONLINEAR;
        if (windowA.isAncestorOf(windowB)) {
            PointerWindowEvent.Detail detailA3 = PointerWindowEvent.Detail.ANCESTOR;
            PointerWindowEvent.Detail detailB3 = PointerWindowEvent.Detail.INFERIOR;
            detailA = detailA3;
            detailB = detailB3;
        } else if (!windowB.isAncestorOf(windowA)) {
            detailA = detailA2;
            detailB = detailB2;
        } else {
            PointerWindowEvent.Detail detailB4 = PointerWindowEvent.Detail.ANCESTOR;
            PointerWindowEvent.Detail detailA4 = PointerWindowEvent.Detail.INFERIOR;
            detailA = detailA4;
            detailB = detailB4;
        }
        Bitmask keyButMask = getKeyButMask();
        short xA2 = this.xServer.pointer.getX();
        short yA2 = this.xServer.pointer.getY();
        FullscreenTransformation fullscreenTransformationB = windowB.getFullscreenTransformation();
        if (fullscreenTransformationB == null) {
            xA = xA2;
            yA = yA2;
        } else {
            short[] transformedPoint = fullscreenTransformationB.transformPointerCoords(xA2, yA2);
            xA = transformedPoint[0];
            yA = transformedPoint[1];
        }
        short[] localPointA = windowA.rootPointToLocal(xA, yA);
        short xB2 = this.xServer.pointer.getX();
        short yB2 = this.xServer.pointer.getY();
        FullscreenTransformation fullscreenTransformationA = windowA.getFullscreenTransformation();
        if (fullscreenTransformationA == null) {
            xB = xB2;
            yB = yB2;
        } else {
            short[] transformedPoint2 = fullscreenTransformationA.transformPointerCoords(xB2, yB2);
            xB = transformedPoint2[0];
            yB = transformedPoint2[1];
        }
        short[] localPointB = windowB.rootPointToLocal(xB, yB);
        short yB3 = yB;
        sendEvent(windowA, 32, new LeaveNotify(detailA, this.xServer.windowManager.rootWindow, windowA, null, xA, yA, localPointA[0], localPointA[1], keyButMask, mode, sameScreenAndFocus));
        PointerWindowEvent.Detail detail = detailB;
        sendEvent(windowB, 16, new EnterNotify(detail, this.xServer.windowManager.rootWindow, windowB, null, xB, yB3, localPointB[0], localPointB[1], keyButMask, mode, sameScreenAndFocus));
    }

    @Override
    public void onPointerButtonPress(Pointer.Button button) {
        short x;
        short y;
        if (this.xServer.isRelativeMouseMovement()) {
            WinHandler winHandler = this.xServer.getWinHandler();
            int wheelDelta = button == Pointer.Button.BUTTON_SCROLL_UP ? 120 : button == Pointer.Button.BUTTON_SCROLL_DOWN ? -120 : 0;
            winHandler.mouseEvent(MouseEventFlags.getFlagFor(button, true), 0, 0, wheelDelta);
            return;
        }
        Window grabWindow = this.xServer.grabManager.getWindow();
        if (grabWindow == null && (grabWindow = this.pointWindow.getAncestorWithEventId(4)) != null) {
            this.xServer.grabManager.activatePointerGrab(grabWindow);
        }
        if (grabWindow != null && grabWindow.attributes.isEnabled()) {
            Bitmask eventMask = createPointerEventMask();
            eventMask.unset(button.flag());
            short x2 = this.xServer.pointer.getX();
            short y2 = this.xServer.pointer.getY();
            FullscreenTransformation fullscreenTransformation = grabWindow.getFullscreenTransformation();
            if (fullscreenTransformation == null) {
                x = x2;
                y = y2;
            } else {
                short[] transformedPoint = fullscreenTransformation.transformPointerCoords(x2, y2);
                x = transformedPoint[0];
                y = transformedPoint[1];
            }
                short[] localPoint = grabWindow.rootPointToLocal(x, y);
            Window child = grabWindow.isAncestorOf(this.pointWindow) ? this.pointWindow : null;
            grabWindow.sendEvent(4, new ButtonPress(button.code(), this.xServer.windowManager.rootWindow, grabWindow, child, x, y, localPoint[0], localPoint[1], eventMask));
        }
    }

    @Override
    public void onPointerButtonRelease(Pointer.Button button) {
        Window ancestorWithEventMask;
        short x;
        short y;
        if (this.xServer.isRelativeMouseMovement()) {
            WinHandler winHandler = this.xServer.getWinHandler();
            winHandler.mouseEvent(MouseEventFlags.getFlagFor(button, false), 0, 0, 0);
            return;
        }
        Bitmask eventMask = createPointerEventMask();
        Window grabWindow = this.xServer.grabManager.getWindow();
        Window window = null;
        if (grabWindow == null || this.xServer.grabManager.isOwnerEvents()) {
            ancestorWithEventMask = this.pointWindow.getAncestorWithEventMask(eventMask);
        } else {
            ancestorWithEventMask = null;
        }
        Window window2 = ancestorWithEventMask;
        if (grabWindow != null || window2 != null) {
            Window eventWindow = window2 != null ? window2 : grabWindow;
            short x2 = this.xServer.pointer.getX();
            short y2 = this.xServer.pointer.getY();
            FullscreenTransformation fullscreenTransformation = eventWindow.getFullscreenTransformation();
            if (fullscreenTransformation == null) {
                x = x2;
                y = y2;
            } else {
                short[] transformedPoint = fullscreenTransformation.transformPointerCoords(x2, y2);
                x = transformedPoint[0];
                y = transformedPoint[1];
            }
            short[] localPoint = eventWindow.rootPointToLocal(x, y);
            if (eventWindow.isAncestorOf(this.pointWindow)) {
                window = this.pointWindow;
            }
            Window child = window;
            ButtonRelease buttonRelease = new ButtonRelease(button.code(), this.xServer.windowManager.rootWindow, eventWindow, child, x, y, localPoint[0], localPoint[1], eventMask);
            sendEvent(window2, eventMask, buttonRelease);
        }
        if (this.xServer.pointer.getButtonMask().isEmpty() && this.xServer.grabManager.isReleaseWithButtons()) {
            this.xServer.grabManager.deactivatePointerGrab();
        }
    }

    @Override
    public void onPointerMove(short x, short y) {
        Window window;
        short x2;
        short y2;
        updatePointWindow();
        Bitmask eventMask = createPointerEventMask();
        Window grabWindow = this.xServer.grabManager.getWindow();
        Window window2 = null;
        if (grabWindow == null || this.xServer.grabManager.isOwnerEvents()) {
            window = this.pointWindow.getAncestorWithEventMask(eventMask);
        } else {
            window = null;
        }
        if (grabWindow != null || window != null) {
            Window eventWindow = window != null ? window : grabWindow;
            FullscreenTransformation fullscreenTransformation = eventWindow.getFullscreenTransformation();
            if (fullscreenTransformation == null) {
                x2 = x;
                y2 = y;
            } else {
                short[] transformedPoint = fullscreenTransformation.transformPointerCoords(x, y);
                x2 = transformedPoint[0];
                y2 = transformedPoint[1];
            }
            short[] localPoint = eventWindow.rootPointToLocal(x2, y2);
            if (eventWindow.isAncestorOf(this.pointWindow)) {
                window2 = this.pointWindow;
            }
            Window child = window2;
            sendEvent(window, eventMask, new MotionNotify(false, this.xServer.windowManager.rootWindow, eventWindow, child, x2, y2, localPoint[0], localPoint[1], getKeyButMask()));
        }
    }

    @Override
    public void onKeyPress(byte keycode, int keysym) {
        Window child;
        Window eventWindow;
        short x;
        short y;
        Window focusedWindow = this.xServer.windowManager.getFocusedWindow();
        if (focusedWindow == null) {
            return;
        }
        updatePointWindow();
        Window eventWindow2 = null;
        if (!focusedWindow.isAncestorOf(this.pointWindow)) {
            child = null;
        } else {
            eventWindow2 = this.pointWindow.getAncestorWithEventId(1, focusedWindow);
            Window child2 = eventWindow2.isAncestorOf(this.pointWindow) ? this.pointWindow : null;
            child = child2;
        }
        if (eventWindow2 != null) {
            eventWindow = eventWindow2;
        } else if (!focusedWindow.hasEventListenerFor(1)) {
            return;
        } else {
            eventWindow = focusedWindow;
        }
        if (eventWindow.attributes.isEnabled()) {
        Bitmask keyButMask = getKeyButMask();
            short x2 = this.xServer.pointer.getX();
            short y2 = this.xServer.pointer.getY();
            FullscreenTransformation fullscreenTransformation = eventWindow.getFullscreenTransformation();
            if (fullscreenTransformation == null) {
                x = x2;
                y = y2;
            } else {
                short[] transformedPoint = fullscreenTransformation.transformPointerCoords(x2, y2);
                x = transformedPoint[0];
                y = transformedPoint[1];
            }
        short[] localPoint = eventWindow.rootPointToLocal(x, y);
            if (keysym != 0 && !this.xServer.keyboard.hasKeysym(keycode, keysym)) {
                this.xServer.keyboard.setKeysyms(keycode, keysym, keysym);
            eventWindow.sendEvent(new MappingNotify(MappingNotify.Request.KEYBOARD, keycode, 1));
        }
            eventWindow.sendEvent(1, new KeyPress(keycode, this.xServer.windowManager.rootWindow, eventWindow, child, x, y, localPoint[0], localPoint[1], keyButMask));
        }
    }

    @Override
    public void onKeyRelease(byte keycode) {
        short x;
        short y;
        Window focusedWindow = this.xServer.windowManager.getFocusedWindow();
        if (focusedWindow == null) {
            return;
        }
        updatePointWindow();
        Window eventWindow = null;
        Window child = null;
        if (focusedWindow.isAncestorOf(this.pointWindow)) {
            eventWindow = this.pointWindow.getAncestorWithEventId(2, focusedWindow);
            child = eventWindow.isAncestorOf(this.pointWindow) ? this.pointWindow : null;
        }
        if (eventWindow == null) {
            if (!focusedWindow.hasEventListenerFor(Event.KEY_RELEASE)) {
                return;
            } else {
                eventWindow = focusedWindow;
            }
        }
        if (eventWindow.attributes.isEnabled()) {
            Bitmask keyButMask = getKeyButMask();
            short x2 = this.xServer.pointer.getX();
            short y2 = this.xServer.pointer.getY();
            FullscreenTransformation fullscreenTransformation = eventWindow.getFullscreenTransformation();
            if (fullscreenTransformation == null) {
                x = x2;
                y = y2;
            } else {
                short[] transformedPoint = fullscreenTransformation.transformPointerCoords(x2, y2);
                x = transformedPoint[0];
                y = transformedPoint[1];
            }
            short[] localPoint = eventWindow.rootPointToLocal(x, y);
            eventWindow.sendEvent(Event.KEY_RELEASE, new KeyRelease(keycode, this.xServer.windowManager.rootWindow, eventWindow, child, x, y, localPoint[0], localPoint[1], keyButMask));
        }
    }

    private Bitmask createPointerEventMask() {
        Bitmask eventMask = new Bitmask();
        eventMask.set(Event.POINTER_MOTION);
        Bitmask buttonMask = this.xServer.pointer.getButtonMask();
        if (!buttonMask.isEmpty()) {
            eventMask.set(Event.BUTTON_MOTION);

            if (buttonMask.isSet(Pointer.Button.BUTTON_LEFT.flag())) {
                eventMask.set(Event.BUTTON1_MOTION);
            }
            if (buttonMask.isSet(Pointer.Button.BUTTON_MIDDLE.flag())) {
                eventMask.set(Event.BUTTON2_MOTION);
            }
            if (buttonMask.isSet(Pointer.Button.BUTTON_RIGHT.flag())) {
                eventMask.set(Event.BUTTON3_MOTION);
            }
            if (buttonMask.isSet(Pointer.Button.BUTTON_SCROLL_UP.flag())) {
                eventMask.set(Event.BUTTON4_MOTION);
            }
            if (buttonMask.isSet(Pointer.Button.BUTTON_SCROLL_DOWN.flag())) {
                eventMask.set(Event.BUTTON5_MOTION);
            }
        }
        return eventMask;
    }

    public Bitmask getKeyButMask() {
        Bitmask keyButMask = new Bitmask();
        keyButMask.join(this.xServer.pointer.getButtonMask());
        keyButMask.join(this.xServer.keyboard.getModifiersMask());
        return keyButMask;
    }
}
