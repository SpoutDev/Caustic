/*
 * This file is part of Caustic Software, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.caustic.software;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.flowpowered.math.matrix.Matrix2f;
import com.flowpowered.math.matrix.Matrix3f;
import com.flowpowered.math.matrix.Matrix4f;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;

import com.flowpowered.caustic.api.gl.Program;
import com.flowpowered.caustic.api.gl.Shader;
import com.flowpowered.caustic.api.gl.Shader.ShaderType;
import com.flowpowered.caustic.api.util.CausticUtil;

/**
 *
 */
public class SoftwareProgram extends Program {
    private final SoftwareRenderer renderer;
    private final Map<ShaderType, SoftwareShader> shaders = new EnumMap<>(ShaderType.class);

    SoftwareProgram(SoftwareRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void attachShader(Shader shader) {
        CausticUtil.checkVersion(this, shader);
        shaders.put(shader.getType(), (SoftwareShader) shader);
    }

    SoftwareShader getShader(ShaderType type) {
        return shaders.get(type);
    }

    @Override
    public void detachShader(Shader shader) {
        final Shader found = shaders.get(shader.getType());
        if (shader.equals(found)) {
            shaders.remove(shader.getType());
        }
    }

    @Override
    public void link() {
        // Nothing to do
    }

    @Override
    public void use() {
        renderer.setProgram(this);
    }

    @Override
    public void bindSampler(int unit) {
        final SoftwareTexture texture = renderer.getTexture(unit);
        if (texture == null) {
            throw new IllegalArgumentException("No texture bound at unit " + unit);
        }
        for (SoftwareShader shader : shaders.values()) {
            shader.getImplementation().bindTexture(unit, texture);
        }
    }

    @Override
    public void setUniform(String name, boolean b) {
        setUniform(name, (Object) b);
    }

    @Override
    public void setUniform(String name, int i) {
        setUniform(name, (Object) i);
    }

    @Override
    public void setUniform(String name, float f) {
        setUniform(name, (Object) f);
    }

    @Override
    public void setUniform(String name, float[] fs) {
        setUniform(name, (Object) fs);
    }

    @Override
    public void setUniform(String name, Vector2f v) {
        setUniform(name, (Object) v);
    }

    @Override
    public void setUniform(String name, Vector2f[] vs) {
        setUniform(name, (Object) vs);
    }

    @Override
    public void setUniform(String name, Vector3f v) {
        setUniform(name, (Object) v);
    }

    @Override
    public void setUniform(String name, Vector3f[] vs) {
        setUniform(name, (Object) vs);
    }

    @Override
    public void setUniform(String name, Vector4f v) {
        setUniform(name, (Object) v);
    }

    @Override
    public void setUniform(String name, Matrix2f m) {
        setUniform(name, (Object) m);
    }

    @Override
    public void setUniform(String name, Matrix3f m) {
        setUniform(name, (Object) m);
    }

    @Override
    public void setUniform(String name, Matrix4f m) {
        setUniform(name, (Object) m);
    }

    private void setUniform(String name, Object o) {
        for (SoftwareShader shader : shaders.values()) {
            shader.getImplementation().setUniform(name, o);
        }
    }

    @Override
    public Collection<SoftwareShader> getShaders() {
        return Collections.unmodifiableCollection(shaders.values());
    }

    @Override
    public Set<String> getUniformNames() {
        final Set<String> names = new HashSet<>();
        for (SoftwareShader shader : shaders.values()) {
            names.addAll(shader.getImplementation().getUniformNames());
        }
        return names;
    }

    @Override
    public GLVersion getGLVersion() {
        return GLVersion.SOFTWARE;
    }
}
