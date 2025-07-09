package com.winlator.renderer;

import com.winlator.xserver.Drawable;

class RenderableWindow {
    final Drawable content;
    final FullscreenTransformation fullscreenTransformation;
    short rootX;
    short rootY;
    final boolean transparent;

    public RenderableWindow(Drawable content, int rootX, int rootY, boolean transparent, FullscreenTransformation fullscreenTransformation) {
        this.content = content;
        this.rootX = (short)rootX;
        this.rootY = (short)rootY;
        this.transparent = transparent;
        this.fullscreenTransformation = fullscreenTransformation;
    }
}
