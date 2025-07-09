package com.winlator.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import androidx.core.graphics.ColorUtils;
import com.winlator.core.Bitmask;
import com.winlator.core.Callback;
import com.winlator.core.ImageUtils;

// import com.winlator.R;
// import com.winlator.XrActivity;
import app.gamenative.R;
import com.winlator.math.Mathf;
import com.winlator.math.XForm;
import com.winlator.renderer.material.CursorMaterial;
import com.winlator.renderer.material.ScreenMaterial;
import com.winlator.renderer.material.WindowMaterial;
import com.winlator.widget.XServerView;
import com.winlator.xserver.Cursor;
import com.winlator.xserver.Decoration;
import com.winlator.xserver.Drawable;
import com.winlator.xserver.Pointer;
import com.winlator.xserver.ScreenInfo;
import com.winlator.xserver.Window;
import com.winlator.xserver.WindowAttributes;
import com.winlator.xserver.WindowManager;
import com.winlator.xserver.XLock;
import com.winlator.xserver.XServer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer, WindowManager.OnWindowModificationListener, Pointer.OnPointerMotionListener {
    private int cursorBackColor;
    private int cursorForeColor;
    private final CursorMaterial cursorMaterial;
    private float cursorScale;
    private boolean cursorVisible;
    public final EffectComposer effectComposer;
    private boolean forceWindowsFullscreen;
    private boolean fullscreen;
    private float magnifierZoom;
    protected final VertexAttribute quadVertices;
    private final ArrayList<RenderableWindow> renderableWindows;
    private final Drawable rootCursorDrawable;
    private boolean screenOffsetYRelativeToCursor;
    protected short surfaceHeight;
    protected short surfaceWidth;
    private final float[] tmpXForm1;
    private final float[] tmpXForm2;
    private boolean toggleFullscreen;
    private String[] unviewableWMClasses;
    public final ViewTransformation viewTransformation;
    protected boolean viewportNeedsUpdate;
    private final WindowMaterial windowMaterial;
    private final XServer xServer;
    public final XServerView xServerView;
    private String forceFullscreenWMClass = null;
    private boolean magnifierEnabled = true;

    public GLRenderer(XServerView xServerView, XServer xServer) {
        VertexAttribute vertexAttribute = new VertexAttribute("position", 2);
        this.quadVertices = vertexAttribute;
        this.tmpXForm1 = XForm.getInstance();
        this.tmpXForm2 = XForm.getInstance();
        this.cursorMaterial = new CursorMaterial();
        this.windowMaterial = new WindowMaterial();
        this.viewTransformation = new ViewTransformation();
        this.renderableWindows = new ArrayList<>();
        this.fullscreen = false;
        this.toggleFullscreen = false;
        this.viewportNeedsUpdate = true;
        this.cursorVisible = true;
        this.cursorScale = 1.0f;
        this.cursorBackColor = 16777215;
        this.cursorForeColor = 0;
        this.screenOffsetYRelativeToCursor = false;
        this.unviewableWMClasses = null;
        this.magnifierZoom = 1.0f;
        this.effectComposer = new EffectComposer(this);
        this.xServerView = xServerView;
        this.xServer = xServer;
        this.rootCursorDrawable = createRootCursorDrawable();
        vertexAttribute.put(new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f});
        xServer.windowManager.addOnWindowModificationListener(this);
        xServer.pointer.addOnPointerMotionListener(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GPUImage.checkIsSupported();
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.surfaceWidth = (short) width;
        this.surfaceHeight = (short) height;
        ViewTransformation viewTransformation = this.viewTransformation;
        ScreenInfo screenInfo = this.xServer.screenInfo;
        viewTransformation.update(width, height, screenInfo.width, screenInfo.height);
        this.viewportNeedsUpdate = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (this.toggleFullscreen) {
            this.fullscreen = !this.fullscreen;
            this.toggleFullscreen = false;
            this.viewportNeedsUpdate = true;
        }
        if (this.effectComposer.hasEffects()) {
            this.effectComposer.render();
        } else {
            drawFrame();
        }
    }

    protected void drawFrame() {
        if (this.viewportNeedsUpdate) {
            if (this.fullscreen) {
                GLES20.glViewport(0, 0, this.surfaceWidth, this.surfaceHeight);
            } else {
                ViewTransformation viewTransformation = this.viewTransformation;
                GLES20.glViewport(viewTransformation.viewOffsetX, viewTransformation.viewOffsetY, viewTransformation.viewWidth, viewTransformation.viewHeight);
            }
            this.viewportNeedsUpdate = false;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        float pointerX = 0.0f;
        float pointerY = 0.0f;
        float magnifierZoom = !this.screenOffsetYRelativeToCursor ? this.magnifierZoom : 1.0f;
        if (magnifierZoom != 1.0f) {
            short s = this.xServer.screenInfo.width;
            pointerX = Mathf.clamp((this.xServer.pointer.getX() * magnifierZoom) - (s * 0.5f), 0.0f, s * Math.abs(1.0f - magnifierZoom));
        }
        if (this.screenOffsetYRelativeToCursor || magnifierZoom != 1.0f) {
                float scaleY = magnifierZoom != 1.0f ? Math.abs(1.0f - magnifierZoom) : 0.5f;
            XServer xServer = this.xServer;
            float offsetY = xServer.screenInfo.height * (this.screenOffsetYRelativeToCursor ? 0.25f : 0.5f);
            pointerY = Mathf.clamp((xServer.pointer.getY() * magnifierZoom) - offsetY, 0.0f, this.xServer.screenInfo.height * scaleY);
            }
        XForm.makeTransform(this.tmpXForm2, -pointerX, -pointerY, magnifierZoom, magnifierZoom, 0.0f);
        renderWindows();
        if (this.cursorVisible) {
            renderCursor();
        }
    }

    @Override
    public void onMapWindow(Window window) {
        xServerView.queueEvent(this::updateScene);
        xServerView.requestRender();
    }

    @Override
    public void onUnmapWindow(Window window) {
        xServerView.queueEvent(this::updateScene);
        xServerView.requestRender();
    }

    @Override
    public void onChangeWindowZOrder(Window window) {
        xServerView.queueEvent(this::updateScene);
        xServerView.requestRender();
    }

    @Override
    public void onUpdateWindowContent(Window window) {
        xServerView.requestRender();
    }

    @Override
    public void onUpdateWindowGeometry(final Window window, boolean resized) {
        if (resized) {
            xServerView.queueEvent(this::updateScene);
        }
        else xServerView.queueEvent(() -> updateWindowPosition(window));
        xServerView.requestRender();
    }

    @Override
    public void onUpdateWindowAttributes(Window window, Bitmask mask) {
        if (mask.isSet(WindowAttributes.FLAG_CURSOR)) xServerView.requestRender();
    }

    @Override
    public void onPointerMove(short x, short y) {
        xServerView.requestRender();
    }

    private void renderCursorDrawable(Drawable drawable, int x, int y) {
        synchronized (drawable.renderLock) {
            Texture texture = drawable.getTexture();
            texture.updateFromDrawable(drawable);
            float f = drawable.width;
            float f2 = this.cursorScale;
            XForm.set(this.tmpXForm1, x, y, f * f2, drawable.height * f2);
            float[] fArr = this.tmpXForm1;
            XForm.multiply(fArr, fArr, this.tmpXForm2);
            this.cursorMaterial.setUniformColor("backColor", this.cursorBackColor);
            this.cursorMaterial.setUniformColor("foreColor", this.cursorForeColor);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, texture.getTextureId());
            this.cursorMaterial.setUniformInt("texture", 0);
            this.cursorMaterial.setUniformFloatArray("xform", this.tmpXForm1);
            GLES20.glDrawArrays(5, 0, this.quadVertices.count());
            GLES20.glBindTexture(3553, 0);
        }
    }

    private void renderWindowDrawable(Drawable drawable, int x, int y, boolean transparent, FullscreenTransformation fullscreenTransformation) {
        synchronized (drawable.renderLock) {
            Texture texture = drawable.getTexture();
            texture.updateFromDrawable(drawable);
            if (fullscreenTransformation != null) {
                XForm.set(this.tmpXForm1, fullscreenTransformation.x, fullscreenTransformation.y, fullscreenTransformation.width, fullscreenTransformation.height);
            } else {
                XForm.set(this.tmpXForm1, x, y, drawable.width, drawable.height);
            }
            float[] fArr = this.tmpXForm1;
            XForm.multiply(fArr, fArr, this.tmpXForm2);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, texture.getTextureId());
            this.windowMaterial.setUniformInt("texture", 0);
            this.windowMaterial.setUniformFloat("noAlpha", !transparent ? 1.0f : 0.0f);
            this.windowMaterial.setUniformFloatArray("xform", this.tmpXForm1);
            GLES20.glDrawArrays(5, 0, this.quadVertices.count());
            GLES20.glBindTexture(3553, 0);
        }
    }

    private void renderWindows() {
        this.windowMaterial.use();
        WindowMaterial windowMaterial = this.windowMaterial;
        ScreenInfo screenInfo = this.xServer.screenInfo;
        windowMaterial.setUniformVec2("viewSize", screenInfo.width, screenInfo.height);
        this.quadVertices.bind(this.windowMaterial.programId);
        XLock lock = this.xServer.lock(XServer.Lockable.DRAWABLE_MANAGER);
        try {
            Iterator<RenderableWindow> it = this.renderableWindows.iterator();
            while (it.hasNext()) {
                RenderableWindow window = it.next();
                if (!window.content.isOffscreenStorage()) {
                    renderWindowDrawable(window.content, window.rootX, window.rootY, window.transparent, window.fullscreenTransformation);
                }
            }
            if (lock != null) {
                lock.close();
            }
            this.quadVertices.disable();
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

    private void renderCursor() {
        this.cursorMaterial.use();
        CursorMaterial cursorMaterial = this.cursorMaterial;
        ScreenInfo screenInfo = this.xServer.screenInfo;
        cursorMaterial.setUniformVec2("viewSize", screenInfo.width, screenInfo.height);
        this.quadVertices.bind(this.cursorMaterial.programId);
        XLock lock = this.xServer.lock(XServer.Lockable.DRAWABLE_MANAGER);
        try {
            Window pointWindow = this.xServer.inputDeviceManager.getPointWindow();
            Cursor cursor = pointWindow != null ? pointWindow.attributes.getCursor() : null;
            short x = this.xServer.pointer.getClampedX();
            short y = this.xServer.pointer.getClampedY();
            if (cursor != null) {
                if (cursor.isVisible()) {
                    renderCursorDrawable(cursor.cursorImage, x - cursor.hotSpotX, y - cursor.hotSpotY);
                }
            } else {
                renderCursorDrawable(this.rootCursorDrawable, x, y);
            }
            if (lock != null) {
                lock.close();
            }
            this.quadVertices.disable();
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

    public void toggleFullscreen() {
        toggleFullscreen = true;
        xServerView.requestRender();
    }

    private Drawable createRootCursorDrawable() {
        Context context = xServerView.getContext();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cursor, options);
        return Drawable.fromBitmap(bitmap);
    }

    private void updateScene() {
        try (XLock lock = xServer.lock(XServer.Lockable.WINDOW_MANAGER, XServer.Lockable.DRAWABLE_MANAGER)) {
            renderableWindows.clear();
            collectRenderableWindows(xServer.windowManager.rootWindow, xServer.windowManager.rootWindow.getX(), xServer.windowManager.rootWindow.getY());
        }
    }

    private void collectRenderableWindows(Window window, int x, int y) {
        FullscreenTransformation fullscreenTransformation;
        if (window.isRenderable()) {
            if (window != this.xServer.windowManager.rootWindow) {
            boolean viewable = true;
                boolean inBounds = false;
                if (this.unviewableWMClasses != null) {
                String wmClass = window.getClassName();
                    String[] strArr = this.unviewableWMClasses;
                    int length = strArr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        String unviewableWMClass = strArr[i];
                        if (!wmClass.contains(unviewableWMClass)) {
                            i++;
                        } else {
                            if (window.attributes.isEnabled()) {
                                window.disableAllDescendants();
                            }
                        viewable = false;
                    }
                }
            }
            if (viewable) {
                    Window parent = window.getParent();
                    boolean transparent = window.attributes.isTransparent() || parent.attributes.isTransparent();
                    if (this.forceWindowsFullscreen) {
                    short width = window.getWidth();
                    short height = window.getHeight();
                        if (width >= 320 && height >= 200) {
                            ScreenInfo screenInfo = this.xServer.screenInfo;
                            if (width < screenInfo.width && height < screenInfo.height) {
                                inBounds = true;
                            }
                        }
                        if (window.getType() == Window.Type.NORMAL && inBounds && window.hasNoDecorations()) {
                            FullscreenTransformation fullscreenTransformation2 = window.getFullscreenTransformation();
                            if (fullscreenTransformation2 == null) {
                                FullscreenTransformation fullscreenTransformation3 = new FullscreenTransformation(window);
                                fullscreenTransformation2 = fullscreenTransformation3;
                                window.setFullscreenTransformation(fullscreenTransformation3);
                            }
                            fullscreenTransformation2.update(this.xServer.screenInfo, window.getWidth(), window.getHeight());
                            if (parent != this.xServer.windowManager.rootWindow && parent.getChildCount() == 1 && parent.hasDecoration(Decoration.BORDER) && parent.hasDecoration(Decoration.TITLE)) {
                                FullscreenTransformation parentFullscreenTransformation = parent.getFullscreenTransformation();
                                if (parentFullscreenTransformation == null) {
                                    FullscreenTransformation fullscreenTransformation4 = new FullscreenTransformation(parent);
                                    parentFullscreenTransformation = fullscreenTransformation4;
                                    parent.setFullscreenTransformation(fullscreenTransformation4);
                                }
                                parentFullscreenTransformation.update(this.xServer.screenInfo, parent.getWidth(), parent.getHeight());
                                removeRenderableWindow(parent);
                            } else {
                                parent.setFullscreenTransformation(null);
                            }
                            fullscreenTransformation = fullscreenTransformation2;
                        } else {
                            window.setFullscreenTransformation(null);
                            fullscreenTransformation = null;
                        }
                        this.renderableWindows.add(new RenderableWindow(window.getContent(), x, y, transparent, fullscreenTransformation));
                    } else {
                        this.renderableWindows.add(new RenderableWindow(window.getContent(), x, y, transparent, null));
                    }
                }
            }
            if (window.attributes.isRenderSubwindows()) {
                for (Window child : window.getChildren()) {
                    collectRenderableWindows(child, child.getX() + x, child.getY() + y);
                }
            }
        }
    }

    private void removeRenderableWindow(Window window) {
        for (int i = 0; i < renderableWindows.size(); i++) {
            if (renderableWindows.get(i).content == window.getContent()) {
                renderableWindows.remove(i);
                break;
            }
        }
    }

    private void updateWindowPosition(Window window) {
        for (RenderableWindow renderableWindow : renderableWindows) {
            if (renderableWindow.content == window.getContent()) {
                renderableWindow.rootX = window.getRootX();
                renderableWindow.rootY = window.getRootY();
                break;
            }
        }
    }

    public void setCursorVisible(boolean cursorVisible) {
        this.cursorVisible = cursorVisible;
        xServerView.requestRender();
    }

    public void setCursorScale(float cursorScale) {
        this.cursorScale = cursorScale;
    }

    public void setCursorColor(int cursorColor) {
        this.cursorBackColor = cursorColor;
        this.cursorForeColor = ColorUtils.calculateLuminance(cursorColor) < 0.5d ? 16777215 : 0;
    }

    public void setScreenOffsetYRelativeToCursor(boolean screenOffsetYRelativeToCursor) {
        this.screenOffsetYRelativeToCursor = screenOffsetYRelativeToCursor;
        this.xServerView.requestRender();
    }

    public void setForceWindowsFullscreen(boolean forceWindowsFullscreen) {
        this.forceWindowsFullscreen = forceWindowsFullscreen;
    }

    public void setUnviewableWMClasses(String... unviewableWMNames) {
        this.unviewableWMClasses = unviewableWMNames;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public float getMagnifierZoom() {
        return magnifierZoom;
    }

    public void setMagnifierZoom(float magnifierZoom) {
        this.magnifierZoom = magnifierZoom;
        this.xServerView.requestRender();
    }

    public int[] getPixelsARGB(int x, int y, int width, int height, boolean flipY) {
        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(x, y, width, height, 6408, 5121, pixelBuffer);
        IntBuffer colors = pixelBuffer.asIntBuffer();
        int[] result = new int[width * height];
        if (flipY) {
            for (int i = 0; i < height; i++) {
                colors.position(((height - i) - 1) * width);
                colors.get(result, i * width, width);
            }
        } else {
            colors.get(result);
        }
        for (int i2 = 0; i2 < result.length; i2++) {
            result[i2] = (result[i2] & (-16711936)) | ((result[i2] & 255) << 16) | ((result[i2] & 16711680) >> 16);
        }
        return result;
    }

    public String getForceFullscreenWMClass() {
        return forceFullscreenWMClass;
    }

    public void setForceFullscreenWMClass(String forceFullscreenWMClass) {
        this.forceFullscreenWMClass = forceFullscreenWMClass;
    }
}
