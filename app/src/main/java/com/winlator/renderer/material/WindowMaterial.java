package com.winlator.renderer.material;

public class WindowMaterial extends ShaderMaterial {
    public WindowMaterial() {
        setUniformNames("xform", "viewSize", "texture", "noAlpha");
    }

    @Override
    protected String getVertexShader() {
        return String.join("\n", "uniform float xform[6];", "uniform vec2 viewSize;", "attribute vec2 position;", "varying vec2 vUV;", "void main() {", "vUV = position;", "vec2 transformedPos = applyXForm(position, xform);", "gl_Position = vec4(2.0 * transformedPos.x / viewSize.x - 1.0, 1.0 - 2.0 * transformedPos.y / viewSize.y, 0.0, 1.0);", "}");
    }

    @Override
    protected String getFragmentShader() {
        return String.join("\n", "precision mediump float;", "uniform sampler2D texture;", "uniform float noAlpha;", "varying vec2 vUV;", "void main() {", "vec4 texelColor = texture2D(texture, vUV);", "gl_FragColor = vec4(texelColor.rgb, max(texelColor.a, noAlpha));", "}");
    }
}
