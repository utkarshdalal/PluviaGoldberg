package com.winlator.xserver;

import android.util.SparseArray;

import com.winlator.core.Bitmask;
import com.winlator.xconnector.XInputStream;
import com.winlator.xserver.Window;
import com.winlator.xserver.WindowAttributes;
import com.winlator.xserver.errors.BadIdChoice;
import com.winlator.xserver.errors.BadMatch;
import com.winlator.xserver.errors.XRequestError;
import com.winlator.xserver.events.ConfigureNotify;
import com.winlator.xserver.events.ConfigureRequest;
import com.winlator.xserver.events.DestroyNotify;
import com.winlator.xserver.events.Event;
import com.winlator.xserver.events.Expose;
import com.winlator.xserver.events.MapNotify;
import com.winlator.xserver.events.MapRequest;
import com.winlator.xserver.events.ResizeRequest;
import com.winlator.xserver.events.UnmapNotify;

import java.util.ArrayList;
import java.util.List;

public class WindowManager extends XResourceManager {
    public final DrawableManager drawableManager;
    private FocusRevertTo focusRevertTo;
    private Window focusedWindow;
    private final ArrayList<OnWindowModificationListener> onWindowModificationListeners;
    public final Window rootWindow;
    private final SparseArray<Window> windows;

    public enum FocusRevertTo {
        NONE,
        POINTER_ROOT,
        PARENT
    }

    public interface OnWindowModificationListener {
        default void onMapWindow(Window window) {}

        default void onUnmapWindow(Window window) {}

        default void onChangeWindowZOrder(Window window) {}

        default void onUpdateWindowContent(Window window) {}

        default void onUpdateWindowGeometry(Window window, boolean resized) {}

        default void onUpdateWindowAttributes(Window window, Bitmask mask) {}

        default void onModifyWindowProperty(Window window, Property property) {}
    }

    public WindowManager(ScreenInfo screenInfo, DrawableManager drawableManager) {
        SparseArray<Window> sparseArray = new SparseArray<>();
        this.windows = sparseArray;
        this.focusRevertTo = FocusRevertTo.NONE;
        this.onWindowModificationListeners = new ArrayList<>();
        this.drawableManager = drawableManager;
        int id = IDGenerator.generate();
        Drawable drawable = drawableManager.createDrawable(id, screenInfo.width, screenInfo.height, drawableManager.getVisual());
        Window window = new Window(id, drawable, 0, 0, screenInfo.width, screenInfo.height, null);
        this.rootWindow = window;
        window.attributes.setMapped(true);
        sparseArray.put(id, window);
    }

    public Window getWindow(int id) {
        return this.windows.get(id);
    }

    public ArrayList<Window> findDialogWindows(int id) {
        ArrayList<Window> result = new ArrayList<>();
        for (int i = 0; i < this.windows.size(); i++) {
            Window window = this.windows.valueAt(i);
            if (window != null && window.getTransientFor() == id) {
                result.add(window);
            }
        }
        return result;
    }

    public Window findWindowWithProcessId(int processId) {
        for (int i = 0; i < this.windows.size(); i++) {
            Window window = this.windows.valueAt(i);
            if (window != null && window.getProcessId() == processId) {
                return window;
            }
        }
        return null;
    }

    public void destroyWindow(int id) {
        Window window = getWindow(id);
        if (window != null && this.rootWindow.id != id) {
            unmapWindow(window);
            removeAllSubwindowsAndWindow(window);
        }
    }

    private void removeAllSubwindowsAndWindow(Window window) {
        List<Window> children = new ArrayList<>(window.getChildren());
        for (Window child : children) {
            removeAllSubwindowsAndWindow(child);
        }
        Window parent = window.getParent();
        window.sendEvent(Event.STRUCTURE_NOTIFY, new DestroyNotify(window, window));
        parent.sendEvent(Event.SUBSTRUCTURE_NOTIFY, new DestroyNotify(parent, window));
        this.windows.remove(window.id);
        if (window.isInputOutput()) {
            this.drawableManager.removeDrawable(window.getContent().id);
        }
        triggerOnFreeResourceListener(window);
        if (window == this.focusedWindow) {
            revertFocus();
        }
        parent.removeChild(window);
    }

    public void mapWindow(Window window) {
        if (!window.attributes.isMapped()) {
            Window parent = window.getParent();
            if (!parent.hasEventListenerFor(Event.SUBSTRUCTURE_REDIRECT) || window.attributes.isOverrideRedirect()) {
                parent.sendEvent(Event.SUBSTRUCTURE_REDIRECT, new MapRequest(parent, window));
                return;
            }
            window.attributes.setMapped(true);
            window.sendEvent(Event.STRUCTURE_NOTIFY, new MapNotify(window, window));
            parent.sendEvent(Event.SUBSTRUCTURE_NOTIFY, new MapNotify(parent, window));
            window.sendEvent(Event.EXPOSURE, new Expose(window));
            triggerOnMapWindow(window);
        }
    }

    public void unmapWindow(Window window) {
        if (this.rootWindow.id != window.id && window.attributes.isMapped()) {
            window.attributes.setMapped(false);
            Window parent = window.getParent();
            window.sendEvent(Event.STRUCTURE_NOTIFY, new UnmapNotify(window, window));
            parent.sendEvent(Event.SUBSTRUCTURE_NOTIFY, new UnmapNotify(parent, window));
            if (window == this.focusedWindow) {
                revertFocus();
            }
            triggerOnUnmapWindow(window);
        }
    }

    public void mapSubWindows(Window window) {
        for (Window child : window.getChildren()) {
            mapSubWindows(child);
        }
        mapWindow(window);
    }

    public Window getFocusedWindow() {
        return this.focusedWindow;
    }

    public void revertFocus() {
        switch (focusRevertTo) {
            case NONE:
                focusedWindow = null;
                break;
            case POINTER_ROOT:
                focusedWindow = rootWindow;
                break;
            case PARENT:
                if (focusedWindow.getParent() != null) focusedWindow = focusedWindow.getParent();
                break;
        }
    }

    public void setFocus(Window focusedWindow, FocusRevertTo focusRevertTo) {
        this.focusedWindow = focusedWindow;
        this.focusRevertTo = focusRevertTo;
    }

    public FocusRevertTo getFocusRevertTo() {
        return this.focusRevertTo;
    }

    public Window createWindow(int id, Window parent, short x, short y, short width, short height, WindowAttributes.WindowClass windowClass, Visual visual, byte depth, XClient client) throws XRequestError {
        boolean isInputOutput;
        byte depth2;
        Visual visual2;
        Drawable drawable;
        if (this.windows.indexOfKey(id) >= 0) {
            throw new BadIdChoice(id);
        }
        switch (windowClass) {
            case COPY_FROM_PARENT:
                byte depth3 = (depth == 0 && parent.isInputOutput()) ? parent.getContent().visual.depth : depth;
                boolean isInputOutput2 = parent.isInputOutput();
                isInputOutput = isInputOutput2;
                depth2 = depth3;
                break;
            case INPUT_OUTPUT:
                if (parent.isInputOutput()) {
                    isInputOutput = true;
                    depth2 = depth == 0 ? parent.getContent().visual.depth : depth;
                    break;
                } else {
                    throw new BadMatch();
                }
            case INPUT_ONLY:
                depth2 = depth;
                isInputOutput = false;
                break;
            default:
                depth2 = depth;
                isInputOutput = false;
                break;
        }
        if (!isInputOutput) {
            visual2 = visual;
        } else {
            Visual visual3 = visual == null ? parent.getContent().visual : visual;
            if (depth2 != visual3.depth) {
                throw new BadMatch();
            }
            visual2 = visual3;
        }
        if (!isInputOutput) {
            drawable = null;
        } else {
            Drawable drawable2 = this.drawableManager.createDrawable(id, width, height, visual2);
            if (drawable2 == null) {
                throw new BadIdChoice(id);
            }
            drawable = drawable2;
        }
        final Window window = new Window(id, drawable, x, y, width, height, client);
        window.attributes.setWindowClass(windowClass);
        if (drawable != null) drawable.setOnDrawListener(() -> triggerOnUpdateWindowContent(window));
        windows.put(id, window);
        parent.addChild(window);
        triggerOnCreateResourceListener(window);
        return window;
    }

    private void changeWindowGeometry(final Window window, short x, short y, short width, short height) {
        boolean resized = (window.getWidth() == width && window.getHeight() == height) ? false : true;
        if (resized && window.hasEventListenerFor(Event.RESIZE_REDIRECT)) {
            window.sendEvent(Event.SUBSTRUCTURE_REDIRECT, new ResizeRequest(window, width, height));
            width = window.getWidth();
            height = window.getHeight();
            resized = false;
        }
        if (resized && window.isInputOutput()) {
            Drawable oldContent = window.getContent();
            this.drawableManager.removeDrawable(oldContent.id);
            Drawable newContent = this.drawableManager.createDrawable(oldContent.id, width, height, oldContent.visual);
            newContent.setOffscreenStorage(oldContent.isOffscreenStorage());
            newContent.setOnDrawListener(() -> triggerOnUpdateWindowContent(window));
            window.setContent(newContent);
        }
        if (resized || window.getX() != x || window.getY() != y) {
            window.setX(x);
            window.setY(y);
            window.setWidth(width);
            window.setHeight(height);
            triggerOnUpdateWindowGeometry(window, resized);
        }
        if (resized && window.isInputOutput() && window.attributes.isMapped()) {
            window.sendEvent(new Expose(window));
        }
    }

    private void changeWindowZOrder(Window.StackMode stackMode, Window window, Window sibling) {
        Window parent = window.getParent();
        switch (stackMode) {
            case ABOVE:
                parent.moveChildAbove(window, sibling);
                break;
            case BELOW:
                parent.moveChildBelow(window, sibling);
                break;
        }
        triggerOnChangeWindowZOrder(window);
    }

    public void configureWindow(Window window, Bitmask valueMask, XInputStream inputStream) {
        short x = window.getX();
        short y = window.getY();
        short width = window.getWidth();
        short height = window.getHeight();
        short borderWidth = window.getBorderWidth();
        short x2 = x;
        short y2 = y;
        short width2 = width;
        short height2 = height;
        Window sibling = null;
        Window.StackMode stackMode = null;

        for (int index : valueMask) {
            switch (index) {
                case Window.FLAG_X:
                    short x3 = (short) inputStream.readInt();
                    x2 = x3;
                    break;
                case Window.FLAG_Y:
                    short y3 = (short) inputStream.readInt();
                    y2 = y3;
                    break;
                case Window.FLAG_WIDTH:
                    short width3 = (short) inputStream.readInt();
                    width2 = width3;
                    break;
                case Window.FLAG_HEIGHT:
                    short height3 = (short) inputStream.readInt();
                    height2 = height3;
                    break;
                case Window.FLAG_BORDER_WIDTH:
                    short borderWidth2 = (short) inputStream.readInt();
                    borderWidth = borderWidth2;
                    break;
                case Window.FLAG_SIBLING:
                    Window sibling2 = getWindow(inputStream.readInt());
                    sibling = sibling2;
                    break;
                case Window.FLAG_STACK_MODE:
                    stackMode = Window.StackMode.values()[inputStream.readInt()];
                    break;
            }
        }
        Window parent = window.getParent();
        boolean overrideRedirect = window.attributes.isOverrideRedirect();
        if (!parent.hasEventListenerFor(Event.SUBSTRUCTURE_REDIRECT) && !overrideRedirect) {
            parent.sendEvent(Event.SUBSTRUCTURE_REDIRECT, new ConfigureRequest(parent, window, window.previousSibling(), x2, y2, width2, height2, borderWidth, stackMode, valueMask));
            return;
        }
        Window sibling3 = sibling;
        Window.StackMode stackMode2 = stackMode;
        Window sibling4 = sibling3;
        short borderWidth3 = borderWidth;
        short borderWidth4 = width2;
        changeWindowGeometry(window, x2, y2, borderWidth4, height2);
        window.setBorderWidth(borderWidth3);
        if (stackMode2 != null) {
            changeWindowZOrder(stackMode2, window, sibling4);
        }
        Window previousSibling = window.previousSibling();
        short s = x2;
        short borderWidth5 = y2;
        short s2 = width2;
        short s3 = height2;
        window.sendEvent(Event.STRUCTURE_NOTIFY, new ConfigureNotify(window, window, previousSibling, s, borderWidth5, s2, s3, borderWidth3, overrideRedirect));
        parent.sendEvent(Event.SUBSTRUCTURE_NOTIFY, new ConfigureNotify(parent, window, previousSibling, s, borderWidth5, s2, s3, borderWidth3, overrideRedirect));
    }

    public void reparentWindow(Window window, Window newParent) {
        Window oldParent = window.getParent();
        if (oldParent != null) oldParent.removeChild(window);
        newParent.addChild(window);
    }

    public Window findPointWindow(short rootX, short rootY) {
        return findPointWindow(this.rootWindow, rootX, rootY, false);
    }

    public Window findPointWindow(short rootX, short rootY, boolean useFullscreenTransformation) {
        return findPointWindow(this.rootWindow, rootX, rootY, useFullscreenTransformation);
    }

    private Window findPointWindow(Window window, short rootX, short rootY, boolean useFullscreenTransformation) {
        if (!window.attributes.isMapped() || !window.containsPoint(rootX, rootY, useFullscreenTransformation)) {
            return null;
        }
        Window child = window.getChildByCoords(rootX, rootY, useFullscreenTransformation);
        return child != null ? findPointWindow(child, rootX, rootY, useFullscreenTransformation) : window;
    }

    public void addOnWindowModificationListener(OnWindowModificationListener onWindowModificationListener) {
        this.onWindowModificationListeners.add(onWindowModificationListener);
    }

    private void triggerOnMapWindow(Window window) {
        for (int i = this.onWindowModificationListeners.size() - 1; i >= 0; i--) {
            this.onWindowModificationListeners.get(i).onMapWindow(window);
        }
    }

    private void triggerOnUnmapWindow(Window window) {
        for (int i = this.onWindowModificationListeners.size() - 1; i >= 0; i--) {
            this.onWindowModificationListeners.get(i).onUnmapWindow(window);
        }
    }

    private void triggerOnChangeWindowZOrder(Window window) {
        for (int i = this.onWindowModificationListeners.size() - 1; i >= 0; i--) {
            this.onWindowModificationListeners.get(i).onChangeWindowZOrder(window);
        }
    }

    protected void triggerOnUpdateWindowContent(Window window) {
        for (int i = onWindowModificationListeners.size()-1; i >= 0; i--) {
            onWindowModificationListeners.get(i).onUpdateWindowContent(window);
        }
    }

    protected void triggerOnUpdateWindowGeometry(Window window, boolean resized) {
        for (int i = onWindowModificationListeners.size()-1; i >= 0; i--) {
            onWindowModificationListeners.get(i).onUpdateWindowGeometry(window, resized);
        }
    }

    public void triggerOnUpdateWindowAttributes(Window window, Bitmask mask) {
        for (int i = onWindowModificationListeners.size()-1; i >= 0; i--) {
            onWindowModificationListeners.get(i).onUpdateWindowAttributes(window, mask);
        }
    }

    public void triggerOnModifyWindowProperty(Window window, Property property) {
        for (int i = onWindowModificationListeners.size()-1; i >= 0; i--) {
            onWindowModificationListeners.get(i).onModifyWindowProperty(window, property);
        }
    }
}
