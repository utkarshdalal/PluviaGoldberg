package com.winlator.renderer.effects;

import com.winlator.renderer.material.ShaderMaterial;

/* loaded from: classes.dex */
public abstract class Effect {
    private ShaderMaterial material;

    protected ShaderMaterial createMaterial() {
        return null;
    }

    public ShaderMaterial getMaterial() {
        if (this.material == null) {
            this.material = createMaterial();
        }
        return this.material;
    }
}
