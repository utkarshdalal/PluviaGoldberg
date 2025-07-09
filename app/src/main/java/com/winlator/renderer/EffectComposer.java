package com.winlator.renderer;

import android.opengl.GLES20;
import com.winlator.renderer.effects.Effect;
import com.winlator.renderer.material.ShaderMaterial;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class EffectComposer {
    private final GLRenderer renderer;
    private RenderTarget readBuffer = null;
    private RenderTarget writeBuffer = null;
    private final ArrayList<Effect> effects = new ArrayList<>();

    public EffectComposer(GLRenderer renderer) {
        this.renderer = renderer;
    }

    public synchronized void addEffect(Effect effect) {
        if (!this.effects.contains(effect)) {
            this.effects.add(effect);
        }
        this.renderer.xServerView.requestRender();
    }

    public synchronized void removeEffect(Effect effect) {
        this.effects.remove(effect);
        this.renderer.xServerView.requestRender();
    }

    public synchronized <T extends Effect> T getEffect(Class<T> effectClass) {
        Iterator<Effect> it = this.effects.iterator();
        while (it.hasNext()) {
            T t = (T) it.next();
            if (t.getClass() == effectClass) {
                return t;
            }
        }
        return null;
    }

    public synchronized boolean hasEffects() {
        return !this.effects.isEmpty();
    }

    private void swapBuffers() {
        RenderTarget tmp = this.writeBuffer;
        this.writeBuffer = this.readBuffer;
        this.readBuffer = tmp;
    }

    private void renderEffect(Effect effect) {
        ShaderMaterial material = effect.getMaterial();
        material.use();
        this.renderer.quadVertices.bind(material.programId);
        GLRenderer gLRenderer = this.renderer;
        material.setUniformVec2("resolution", gLRenderer.surfaceWidth, gLRenderer.surfaceHeight);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.readBuffer.getTextureId());
        material.setUniformInt("screenTexture", 0);
        GLES20.glDrawArrays(5, 0, this.renderer.quadVertices.count());
        GLES20.glBindTexture(3553, 0);
    }

    private void initBuffers() {
        if (this.readBuffer == null) {
            RenderTarget renderTarget = new RenderTarget();
            this.readBuffer = renderTarget;
            GLRenderer gLRenderer = this.renderer;
            renderTarget.allocateFramebuffer(gLRenderer.surfaceWidth, gLRenderer.surfaceHeight);
        }
        if (this.writeBuffer == null) {
            RenderTarget renderTarget2 = new RenderTarget();
            this.writeBuffer = renderTarget2;
            GLRenderer gLRenderer2 = this.renderer;
            renderTarget2.allocateFramebuffer(gLRenderer2.surfaceWidth, gLRenderer2.surfaceHeight);
        }
    }

    public synchronized void render() {
        initBuffers();
        GLES20.glBindFramebuffer(36160, hasEffects() ? this.readBuffer.getFramebuffer() : 0);
        this.renderer.drawFrame();
        Iterator<Effect> it = this.effects.iterator();
        while (it.hasNext()) {
            Effect effect = it.next();
            ArrayList<Effect> arrayList = this.effects;
            boolean renderToScreen = effect == arrayList.get(arrayList.size() - 1);
            GLES20.glBindFramebuffer(36160, !renderToScreen ? this.writeBuffer.getFramebuffer() : 0);
            GLRenderer gLRenderer = this.renderer;
            GLES20.glViewport(0, 0, gLRenderer.surfaceWidth, gLRenderer.surfaceHeight);
            this.renderer.viewportNeedsUpdate = true;
            GLES20.glClear(16384);
            renderEffect(effect);
            swapBuffers();
        }
    }
}
