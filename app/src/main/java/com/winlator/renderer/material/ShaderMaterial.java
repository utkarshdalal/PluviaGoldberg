package com.winlator.renderer.material;

import android.graphics.Color;
import android.opengl.GLES20;

import androidx.collection.ArrayMap;

public class ShaderMaterial {
    public int programId;
    private final ArrayMap<String, Integer> uniforms = new ArrayMap<>();

    protected String getFragmentShader() {
        throw null;
    }

    protected String getVertexShader() {
        throw null;
    }

    public void setUniformNames(String... names) {
        this.uniforms.clear();
        for (String name : names) {
            this.uniforms.put(name, -1);
        }
    }

    protected static int compileShaders(String vertexShader, String fragmentShader) {
        int beginIndex = vertexShader.indexOf("void main() {");
        String vertexShader2 = vertexShader.substring(0, beginIndex) + "vec2 applyXForm(vec2 p, float xform[6]) {\nreturn vec2(xform[0] * p.x + xform[2] * p.y + xform[4], xform[1] * p.x + xform[3] * p.y + xform[5]);\n}\n" + vertexShader.substring(beginIndex);
        int programId = GLES20.glCreateProgram();
        int[] compiled = new int[1];
        int vertexShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderId, vertexShader2);
        GLES20.glCompileShader(vertexShaderId);
        GLES20.glGetShaderiv(vertexShaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            throw new RuntimeException("Could not compile vertex shader: \n" + GLES20.glGetShaderInfoLog(vertexShaderId));
        }
        GLES20.glAttachShader(programId, vertexShaderId);
        int fragmentShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderId, fragmentShader);
        GLES20.glCompileShader(fragmentShaderId);
        GLES20.glGetShaderiv(fragmentShaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            throw new RuntimeException("Could not compile fragment shader: \n" + GLES20.glGetShaderInfoLog(fragmentShaderId));
        }
        GLES20.glAttachShader(programId, fragmentShaderId);
        GLES20.glLinkProgram(programId);
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);
        return programId;
    }

    public void use() {
        if (this.programId == 0) {
            this.programId = compileShaders(getVertexShader(), getFragmentShader());
        }
        GLES20.glUseProgram(this.programId);
        for (int i = 0; i < this.uniforms.size(); i++) {
            int location = this.uniforms.valueAt(i).intValue();
            if (location == -1) {
                String name = this.uniforms.keyAt(i);
                this.uniforms.put(name, Integer.valueOf(GLES20.glGetUniformLocation(this.programId, name)));
            }
        }
    }

    public int getUniformLocation(String name) {
        Integer location = this.uniforms.get(name);
        if (location != null) {
            return location.intValue();
        }
        return -1;
    }

    public void destroy() {
        GLES20.glDeleteProgram(this.programId);
        this.programId = 0;
    }

    public void setUniformColor(String uniformName, int color) {
        int location = getUniformLocation(uniformName);
        if (location != -1) {
            GLES20.glUniform3f(location, Color.red(color) * 0.003921569f, Color.green(color) * 0.003921569f, Color.blue(color) * 0.003921569f);
        }
    }

    public void setUniformFloat(String uniformName, float value) {
        int location = getUniformLocation(uniformName);
        if (location != -1) {
            GLES20.glUniform1f(location, value);
        }
    }

    public void setUniformFloatArray(String uniformName, float[] values) {
        int location = getUniformLocation(uniformName);
        if (location != -1) {
            GLES20.glUniform1fv(location, values.length, values, 0);
        }
    }

    public void setUniformInt(String uniformName, int value) {
        int location = getUniformLocation(uniformName);
        if (location != -1) {
            GLES20.glUniform1i(location, value);
        }
    }

    public void setUniformVec2(String uniformName, float x, float y) {
        int location = getUniformLocation(uniformName);
        if (location != -1) {
            GLES20.glUniform2f(location, x, y);
        }
    }
}
