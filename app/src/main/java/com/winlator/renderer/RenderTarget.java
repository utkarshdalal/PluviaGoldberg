package com.winlator.renderer;

import android.opengl.GLES20;

public class RenderTarget extends Texture {
    private int framebuffer;

    private void generateFramebuffer() {
        int[] framebuffers = new int[1];
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        this.framebuffer = framebuffers[0];
    }

    public void allocateFramebuffer(int width, int height) {
        if (this.framebuffer != 0) {
            return;
        }
        generateFramebuffer();
        generateTextureId();
        GLES20.glBindFramebuffer(36160, this.framebuffer);
        GLES20.glActiveTexture(33984);
        GLES20.glPixelStorei(3317, this.unpackAlignment);
        GLES20.glBindTexture(3553, this.textureId);
        int i = this.format;
        GLES20.glTexImage2D(3553, 0, i, width, height, 0, i, 5121, null);
        setTextureParameters();
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.textureId, 0);
        GLES20.glBindTexture(3553, 0);
        GLES20.glBindFramebuffer(36160, 0);
    }

    public int getFramebuffer() {
        return this.framebuffer;
    }
}
