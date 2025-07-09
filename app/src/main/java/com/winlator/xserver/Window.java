package com.winlator.xserver;

import android.util.ArrayMap;
import android.util.SparseArray;
import com.winlator.core.Bitmask;
import com.winlator.renderer.FullscreenTransformation;
import com.winlator.xserver.Property;
import com.winlator.xserver.events.Event;
import com.winlator.xserver.events.PropertyNotify;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Window extends XResource {
    public static final int FLAG_X = 1;
    public static final int FLAG_Y = 1<<1;
    public static final int FLAG_WIDTH = 1<<2;
    public static final int FLAG_HEIGHT = 1<<3;
    public static final int FLAG_BORDER_WIDTH = 1<<4;
    public static final int FLAG_SIBLING = 1<<5;
    public static final int FLAG_STACK_MODE = 1<<6;
    public final WindowAttributes attributes;
    private short borderWidth;
    private final ArrayList<Window> children;
    private Drawable content;
    private final ArrayList<EventListener> eventListeners;
    private FullscreenTransformation fullscreenTransformation;
    private short height;
    private final List<Window> immutableChildren;
    public final XClient originClient;
    private Window parent;
    private final SparseArray<Property> properties;
    private ArrayMap<String, Object> tags;
    private short width;
    private short x;
    private short y;

    public enum MapState {
        UNMAPPED,
        UNVIEWABLE,
        VIEWABLE
    }

    public enum StackMode {
        ABOVE,
        BELOW,
        TOP_IF,
        BOTTOM_IF,
        OPPOSITE
    }

    public enum Type {
        NORMAL,
        DIALOG
    }

    public enum WMHints {
        FLAGS,
        INPUT,
        INITIAL_STATE,
        ICON_PIXMAP,
        ICON_WINDOW,
        ICON_X,
        ICON_Y,
        ICON_MASK,
        WINDOW_GROUP
    }

    public Window(int id, Drawable content, int x, int y, int width, int height, XClient originClient) {
        super(id);
        this.attributes = new WindowAttributes(this);
        this.properties = new SparseArray<>();
        ArrayList<Window> arrayList = new ArrayList<>();
        this.children = arrayList;
        this.immutableChildren = Collections.unmodifiableList(arrayList);
        this.eventListeners = new ArrayList<>();
        this.content = content;
        this.x = (short)x;
        this.y = (short)y;
        this.width = (short)width;
        this.height = (short)height;
        this.originClient = originClient;
    }

    public short getX() {
        return this.x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return this.y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getWidth() {
        return this.width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return this.height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public short getBorderWidth() {
        return this.borderWidth;
    }

    public void setBorderWidth(short borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setTag(String key, Object value) {
        ArrayMap<String, Object> arrayMap = this.tags;
        if (arrayMap == null) {
            arrayMap = new ArrayMap<>();
            this.tags = arrayMap;
        }
        arrayMap.put(key, value);
    }

    public Object getTag(String key, Object fallback) {
        ArrayMap<String, Object> arrayMap = this.tags;
        if (arrayMap == null) {
            arrayMap = new ArrayMap<>();
            this.tags = arrayMap;
        }
        return arrayMap.getOrDefault(key, fallback);
    }

    public Drawable getContent() {
        return this.content;
    }

    public void setContent(Drawable content) {
        this.content = content;
    }

    public Window getParent() {
        return this.parent;
    }

    public Property getProperty(int id) {
        return this.properties.get(id);
    }

    public void addProperty(Property property) {
        this.properties.put(property.name, property);
    }

    public void removeProperty(int id) {
        this.properties.remove(id);
        sendEvent(Event.PROPERTY_CHANGE, new PropertyNotify(this, id, true));
    }

    public Property modifyProperty(int atom, int type, Property.Format format, Property.Mode mode, byte[] data) {
        Property property = getProperty(atom);
        boolean modified = false;
        if (property == null) {
            Property property2 = new Property(atom, type, format, data);
            property = property2;
            addProperty(property2);
            modified = true;
        } else if (mode == Property.Mode.REPLACE) {
            if (property.format == format) {
                property.replace(data);
            } else {
                this.properties.put(atom, new Property(atom, type, format, data));
            }
            modified = true;
        } else if (property.format == format && property.type == type) {
            if (mode == Property.Mode.PREPEND) {
                property.prepend(data);
            } else if (mode == Property.Mode.APPEND) {
                property.append(data);
            }
            modified = true;
        }
        if (modified) {
            sendEvent(Event.PROPERTY_CHANGE, new PropertyNotify(this, atom, false));
            return property;
        }
        return null;
    }

    public String getName() {
        Property property = getProperty(Atom.getId("WM_NAME"));
        return property != null ? property.toString() : "";
    }

    public String getClassName() {
        Property property = getProperty(Atom.getId("WM_CLASS"));
        return property != null ? property.toString() : "";
    }

    public int getWMHintsValue(WMHints wmHints) {
        Property property = getProperty(Atom.getId("WM_HINTS"));
        if (property != null) {
            return property.getInt(wmHints.ordinal());
        }
        return 0;
    }

    public int getProcessId() {
        Property property = getProperty(Atom.getId("_NET_WM_PID"));
        if (property != null) {
            return property.getInt(0);
        }
        return 0;
    }

    public int getTransientFor() {
        Property property = getProperty(Atom.getId("WM_TRANSIENT_FOR"));
        if (property != null) {
            return property.getInt(0);
        }
        return 0;
    }

    public boolean isWoW64() {
        Property property = getProperty(Atom.getId("_NET_WM_WOW64"));
        return property != null && property.data.get(0) == 1;
    }

    public boolean isSurface() {
        Property property = getProperty(Atom.getId("_NET_WM_SURFACE"));
        return property != null && property.data.get(0) == 1;
    }

    public boolean isDesktopWindow() {
        return getClassName().equals("explorer.exe");
    }

    public boolean isDialogBox() {
        return getType() == Type.DIALOG && getTransientFor() > 0 && hasDecoration(Decoration.TITLE) && !(hasDecoration(Decoration.MINIMIZE) && hasDecoration(Decoration.MAXIMIZE));
    }

    public Bitmask getDecorations() {
        Property property = getProperty(Atom.getId("_MOTIF_WM_HINTS"));
        return new Bitmask(property != null ? property.getInt(2) : 0);
    }

    public boolean hasNoDecorations() {
        return getDecorations().isEmpty();
    }

    public boolean hasDecoration(Decoration decoration) {
        return getDecorations().isSet(decoration.flag());
    }

    public Type getType() {
        Property property = getProperty(Atom.getId("_NET_WM_WINDOW_TYPE"));
        return (property == null || !property.toString().equals("_NET_WM_WINDOW_TYPE_DIALOG")) ? Type.NORMAL : Type.DIALOG;
    }

    public long getHandle() {
        Property property = getProperty(Atom.getId("_NET_WM_HWND"));
        if (property != null) {
            return property.getLong(0);
        }
        return 0L;
    }

    public boolean isApplicationWindow() {
        int windowGroup = getWMHintsValue(WMHints.WINDOW_GROUP);
        return isRenderable() && !getName().isEmpty() && windowGroup == this.id;
    }

    public boolean isInputOutput() {
        return this.content != null;
    }

    public void addChild(Window child) {
        if (child == null || child.parent == this) {
            return;
        }
        child.parent = this;
        this.children.add(child);
    }

    public void removeChild(Window child) {
        if (child == null || child.parent != this) {
            return;
        }
        child.parent = null;
        this.children.remove(child);
    }

    public Window previousSibling() {
        int index;
        Window window = this.parent;
        if (window != null && (index = window.children.indexOf(this)) > 0) {
            return this.parent.children.get(index - 1);
        }
        return null;
    }

    public void moveChildAbove(Window child, Window sibling) {
        this.children.remove(child);
        if (sibling != null && this.children.contains(sibling)) {
            ArrayList<Window> arrayList = this.children;
            arrayList.add(arrayList.indexOf(sibling) + 1, child);
        } else {
            this.children.add(child);
        }
    }

    public void moveChildBelow(Window child, Window sibling) {
        this.children.remove(child);
        if (sibling != null && this.children.contains(sibling)) {
            ArrayList<Window> arrayList = this.children;
            arrayList.add(arrayList.indexOf(sibling), child);
        } else {
            this.children.add(0, child);
        }
    }

    public List<Window> getChildren() {
        return this.immutableChildren;
    }

    public int getChildCount() {
        return this.children.size();
    }

    public void addEventListener(EventListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    public void removeEventListener(EventListener eventListener) {
        this.eventListeners.remove(eventListener);
    }

    public boolean hasEventListenerFor(int eventId) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(eventId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEventListenerFor(Bitmask mask) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(mask)) {
                return true;
            }
        }
        return false;
    }

    public void sendEvent(int eventId, Event event) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(eventId)) {
                eventListener.sendEvent(event);
            }
        }
    }

    public void sendEvent(Bitmask eventMask, Event event) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(eventMask)) {
                eventListener.sendEvent(event);
            }
        }
    }

    public void sendEvent(int eventId, Event event, XClient client) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(eventId) && eventListener.client == client) {
                eventListener.sendEvent(event);
            }
        }
    }

    public void sendEvent(Bitmask eventMask, Event event, XClient client) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(eventMask) && eventListener.client == client) {
                eventListener.sendEvent(event);
            }
        }
    }

    public void sendEvent(Event event) {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            eventListener.sendEvent(event);
        }
    }

    public boolean isRenderable() {
        return this.attributes.isMapped() && this.width > 1 && this.height > 1;
    }

    public boolean containsPoint(short rootX, short rootY, boolean useFullscreenTransformation) {
        short[] localPoint = rootPointToLocal(rootX, rootY, useFullscreenTransformation);
        FullscreenTransformation fullscreenTransformation = this.fullscreenTransformation;
        short width = (fullscreenTransformation == null || !useFullscreenTransformation) ? this.width : fullscreenTransformation.width;
        short height = (fullscreenTransformation == null || !useFullscreenTransformation) ? this.height : fullscreenTransformation.height;
        return localPoint[0] >= 0 && localPoint[1] >= 0 && localPoint[0] <= width && localPoint[1] <= height;
    }

    public short[] rootPointToLocal(short x, short y) {
        return rootPointToLocal(x, y, false);
    }

    public short[] rootPointToLocal(short x, short y, boolean useFullscreenTransformation) {
        for (Window window = this; window != null; window = window.parent) {
            FullscreenTransformation fullscreenTransformation = window.fullscreenTransformation;
            x = (short) (x - ((fullscreenTransformation == null || !useFullscreenTransformation) ? window.x : fullscreenTransformation.x));
            y = (short) (y - ((fullscreenTransformation == null || !useFullscreenTransformation) ? window.y : fullscreenTransformation.y));
        }
        return new short[]{x, y};
    }

    public short[] localPointToRoot(short x, short y) {
        return localPointToRoot(x, y, false);
    }

    public short[] localPointToRoot(short x, short y, boolean useFullscreenTransformation) {
        for (Window window = this; window != null; window = window.parent) {
            FullscreenTransformation fullscreenTransformation = window.fullscreenTransformation;
            x = (short) (((fullscreenTransformation == null || !useFullscreenTransformation) ? window.x : fullscreenTransformation.x) + x);
            y = (short) (((fullscreenTransformation == null || !useFullscreenTransformation) ? window.y : fullscreenTransformation.y) + y);
        }
        return new short[]{x, y};
    }

    public short getRootX() {
        return getRootX(false);
    }

    public short getRootX(boolean useFullscreenTransformation) {
        FullscreenTransformation fullscreenTransformation = this.fullscreenTransformation;
        short rootX = (fullscreenTransformation == null || !useFullscreenTransformation) ? this.x : fullscreenTransformation.x;
        for (Window window = this.parent; window != null; window = window.parent) {
            FullscreenTransformation fullscreenTransformation2 = window.fullscreenTransformation;
            rootX = (short) (((fullscreenTransformation2 == null || !useFullscreenTransformation) ? window.x : fullscreenTransformation2.x) + rootX);
        }
        return rootX;
    }

    public short getRootY() {
        return getRootY(false);
    }

    public short getRootY(boolean useFullscreenTransformation) {
        FullscreenTransformation fullscreenTransformation = this.fullscreenTransformation;
        short rootY = (fullscreenTransformation == null || !useFullscreenTransformation) ? this.y : fullscreenTransformation.y;
        for (Window window = this.parent; window != null; window = window.parent) {
            FullscreenTransformation fullscreenTransformation2 = window.fullscreenTransformation;
            rootY = (short) (((fullscreenTransformation2 == null || !useFullscreenTransformation) ? window.y : fullscreenTransformation2.y) + rootY);
        }
        return rootY;
    }

    public Window getAncestorWithEventMask(Bitmask eventMask) {
        for (Window window = this; window != null; window = window.parent) {
            if (window.hasEventListenerFor(eventMask)) {
                return window;
            }
            if (window.attributes.getDoNotPropagateMask().intersects(eventMask)) {
                return null;
            }
        }
        return null;
    }

    public Window getAncestorWithEventId(int eventId) {
        return getAncestorWithEventId(eventId, null);
    }

    public Window getAncestorWithEventId(int eventId, Window endWindow) {
        for (Window window = this; window != null; window = window.parent) {
            if (window.hasEventListenerFor(eventId)) {
                return window;
            }
            if (window == endWindow || window.attributes.getDoNotPropagateMask().isSet(eventId)) {
                return null;
            }
        }
        return null;
    }

    public boolean isAncestorOf(Window window) {
        if (window == this) {
            return false;
        }
        while (window != null) {
            if (window == this) {
                return true;
            }
            window = window.parent;
        }
        return false;
    }

    public Window getChildByCoords(short x, short y) {
        return getChildByCoords(x, y, false);
    }

    public Window getChildByCoords(short x, short y, boolean useFullscreenTransformation) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            Window child = this.children.get(i);
            if (child.attributes.isMapped() && child.containsPoint(x, y, useFullscreenTransformation)) {
                return child;
            }
        }
        return null;
    }

    public MapState getMapState() {
        if (!this.attributes.isMapped()) {
            return MapState.UNMAPPED;
        }
        Window window = this;
        do {
            window = window.parent;
            if (window == null) {
                return MapState.VIEWABLE;
        }
        } while (window.attributes.isMapped());
        return MapState.UNVIEWABLE;
    }

    public Bitmask getAllEventMasks() {
        Bitmask eventMask = new Bitmask();
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            eventMask.join(eventListener.eventMask);
        }
        return eventMask;
    }

    public EventListener getButtonPressListener() {
        Iterator<EventListener> it = this.eventListeners.iterator();
        while (it.hasNext()) {
            EventListener eventListener = it.next();
            if (eventListener.isInterestedIn(4)) {
                return eventListener;
            }
        }
        return null;
    }

    public void disableAllDescendants() {
        Stack<Window> stack = new Stack<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            Window window = stack.pop();
            window.attributes.setEnabled(false);
            stack.addAll(window.children);
        }
    }

    public FullscreenTransformation getFullscreenTransformation() {
        return this.fullscreenTransformation;
        }

    public void setFullscreenTransformation(FullscreenTransformation fullscreenTransformation) {
        this.fullscreenTransformation = fullscreenTransformation;
    }

    public boolean isIconic() {
        return getWMHintsValue(WMHints.INITIAL_STATE) == 3;
    }
}
