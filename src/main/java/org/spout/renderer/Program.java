/*
 * This file is part of Caustic.
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 * Caustic is licensed under the Spout License Version 1.
 *
 * Caustic is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Caustic is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.renderer;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.spout.renderer.Shader.ShaderType;
import org.spout.renderer.data.UniformHolder;

/**
 * Represents a shader program for OpenGL. A program holds the necessary shaders for the rendering
 * pipeline. The GL20 and GL30 versions require at least that sources for the {@link
 * ShaderType#VERTEX} and {@link ShaderType#FRAGMENT} shaders be set before creation. When using
 * GL20, it is strongly recommended to set the attribute layout in the program, which must be done
 * before creation. The layout allows for association between the attribute index in the vertex data
 * and the name in the shaders. For GL30, it is recommended to do so in the shaders instead, using
 * the "layout" keyword. Failing to do so might result in partial, wrong or missing rendering, and affects
 * models using multiple attributes.
 */
public abstract class Program extends Creatable {
	protected int id;
	// Shader sources
	protected Map<ShaderType, InputStream> shaderSources;
	// Map of the attribute names to their vao index (optional for GL30 as they can be defined in the shader instead)
	protected TObjectIntMap<String> attributeLayouts;

	@Override
	public void create() {
		shaderSources = null;
		attributeLayouts = null;
		super.create();
	}

	@Override
	public void destroy() {
		id = 0;
		super.destroy();
	}

	/**
	 * Binds this program to the OpenGL context.
	 */
	public abstract void bind();

	/**
	 * Unbinds this program from the OpenGL context.
	 */
	public abstract void unbind();

	/**
	 * Uploads the uniforms to this program.
	 *
	 * @param uniforms The uniforms to upload
	 */
	public abstract void upload(UniformHolder uniforms);

	/**
	 * Gets the ID for this program as assigned by OpenGL.
	 *
	 * @return The ID
	 */
	public int getID() {
		return id;
	}

	/**
	 * Sets the source of the shader for the type.
	 *
	 * @param type The target type
	 * @param source The source to set
	 */
	public void addShaderSource(ShaderType type, InputStream source) {
		if (shaderSources == null) {
			shaderSources = new EnumMap<>(ShaderType.class);
		}
		shaderSources.put(type, source);
	}

	/**
	 * Returns true if a shader source is present for the type.
	 *
	 * @param type The type to check
	 * @return Whether or not a shader source is present
	 */
	public boolean hasShaderSource(ShaderType type) {
		return shaderSources != null && shaderSources.containsKey(type);
	}

	/**
	 * Returns the shader source for the type, or null if none has been set.
	 *
	 * @param type The to lookup
	 * @return The shader source
	 */
	public InputStream getShaderSource(ShaderType type) {
		return shaderSources != null ? shaderSources.get(type) : null;
	}

	/**
	 * Removes the shader source associated to the type, if present.
	 *
	 * @param type The type to remove
	 */
	public void removeShaderSource(ShaderType type) {
		if (shaderSources != null) {
			shaderSources.remove(type);
		}
	}

	/**
	 * Sets the index of the attribute of the provided name, in the program.
	 *
	 * @param name The name of the attribute
	 * @param index The index for the attribute
	 */
	public void addAttributeLayout(String name, int index) {
		if (attributeLayouts == null) {
			attributeLayouts = new TObjectIntHashMap<>();
		}
		attributeLayouts.put(name, index);
	}

	/**
	 * Returns true if an attribute of the provided name has been set to an index.
	 *
	 * @param name The name to lookup
	 * @return Whether or not the layout is set for the attribute
	 */
	public boolean hasAttributeLayout(String name) {
		return attributeLayouts != null && attributeLayouts.containsKey(name);
	}

	/**
	 * Returns the index for the attribute of the provided name.
	 *
	 * @param name The name to lookup
	 * @return The index
	 */
	public int getAttributeLayout(String name) {
		return attributeLayouts != null ? attributeLayouts.get(name) : -1;
	}

	/**
	 * Removes the index for the attribute of the provided name.
	 */
	public void removeAttributeLayout(String name) {
		if (attributeLayouts != null) {
			attributeLayouts.remove(name);
		}
	}
}
