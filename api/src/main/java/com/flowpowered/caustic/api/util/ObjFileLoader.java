/*
 * This file is part of Caustic API, licensed under the MIT License (MIT).
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
package com.flowpowered.caustic.api.util;

import java.io.InputStream;
import java.util.Scanner;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import com.flowpowered.math.vector.Vector3i;

/**
 * A static loading class for standard .obj model files. This class will load positions, normals can texture coordinates. Missing normals are not calculated. Normals are expected to be of unit length.
 * Models should be triangulated.
 */
public final class ObjFileLoader {
    private ObjFileLoader() {
    }

    private static final String COMPONENT_SEPARATOR = " ";
    private static final String INDEX_SEPARATOR = "/";
    private static final String POSITION_LIST_PREFIX = "v";
    private static final String TEXTURE_LIST_PREFIX = "vt";
    private static final String NORMAL_LIST_PREFIX = "vn";
    private static final String INDEX_LIST_PREFIX = "f";

    /**
     * Loads a .obj file, storing the data in the provided lists. After loading, the input stream will be closed. The number of components for each attribute is returned in a Vector3, x being the
     * number of position components, y the number of normal components and z the number of texture coord components. Note that normal and/or texture coord attributes might be missing from the .obj
     * file. If this is the case, their lists will be empty. Passing null lists for the texture coords or normals will result in no loading of their data. The indices are stored in the indices list.
     *
     * @param stream The input stream for the .obj file
     * @param positions The list in which to store the positions
     * @param normals The list in which to store the normals or null to ignore them
     * @param textureCoords The list in which to store the texture coords
     * @param indices The list in which to store the indices or null to ignore them
     * @return A Vector3 containing, in order, the number of components for the positions, normals and texture coords
     * @throws MalformedObjFileException If any errors occur during loading
     */
    public static Vector3i load(InputStream stream, TFloatList positions, TFloatList normals, TFloatList textureCoords, TIntList indices) {
        int positionSize = -1;
        final TFloatList rawTextureCoords = new TFloatArrayList();
        int textureCoordSize = -1;
        final TFloatList rawNormalComponents = new TFloatArrayList();
        int normalSize = -1;
        final TIntList textureCoordIndices = new TIntArrayList();
        final TIntList normalIndices = new TIntArrayList();
        String line = null;
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith(POSITION_LIST_PREFIX + COMPONENT_SEPARATOR)) {
                    parseComponents(positions, line);
                    if (positionSize == -1) {
                        positionSize = positions.size();
                    }
                } else if (textureCoords != null && line.startsWith(TEXTURE_LIST_PREFIX + COMPONENT_SEPARATOR)) {
                    parseComponents(rawTextureCoords, line);
                    if (textureCoordSize == -1) {
                        textureCoordSize = rawTextureCoords.size();
                    }
                } else if (normals != null && line.startsWith(NORMAL_LIST_PREFIX + COMPONENT_SEPARATOR)) {
                    parseComponents(rawNormalComponents, line);
                    if (normalSize == -1) {
                        normalSize = rawNormalComponents.size();
                    }
                } else if (line.startsWith(INDEX_LIST_PREFIX + COMPONENT_SEPARATOR)) {
                    parseIndices(indices, textureCoordIndices, normalIndices, line);
                }
            }
            line = null;
            final boolean hasTextureCoords;
            final boolean hasNormals;
            if (!textureCoordIndices.isEmpty() && !rawTextureCoords.isEmpty()) {
                textureCoords.fill(0, positions.size() / positionSize * textureCoordSize, 0);
                hasTextureCoords = true;
            } else {
                hasTextureCoords = false;
            }
            if (!normalIndices.isEmpty() && !rawNormalComponents.isEmpty()) {
                normals.fill(0, positions.size() / positionSize * normalSize, 0);
                hasNormals = true;
            } else {
                hasNormals = false;
            }
            if (hasTextureCoords) {
                for (int i = 0; i < textureCoordIndices.size(); i++) {
                    final int textureCoordIndex = textureCoordIndices.get(i) * textureCoordSize;
                    final int positionIndex = indices.get(i) * textureCoordSize;
                    for (int ii = 0; ii < textureCoordSize; ii++) {
                        textureCoords.set(positionIndex + ii, rawTextureCoords.get(textureCoordIndex + ii));
                    }
                }
            }
            if (hasNormals) {
                for (int i = 0; i < normalIndices.size(); i++) {
                    final int normalIndex = normalIndices.get(i) * normalSize;
                    final int positionIndex = indices.get(i) * normalSize;
                    for (int ii = 0; ii < normalSize; ii++) {
                        normals.set(positionIndex + ii, rawNormalComponents.get(normalIndex + ii));
                    }
                }
            }
        } catch (Exception ex) {
            throw new MalformedObjFileException(line, ex);
        }
        return new Vector3i(positionSize, normalSize, textureCoordSize).max(0, 0, 0);
    }

    private static void parseComponents(TFloatList destination, String line) {
        final String[] components = line.split(COMPONENT_SEPARATOR);
        for (int i = 1; i < components.length; i++) {
            destination.add(Float.parseFloat(components[i]));
        }
    }

    private static void parseIndices(TIntList positions, TIntList textureCoords, TIntList normals, String line) {
        final String[] indicesGroup = line.split(COMPONENT_SEPARATOR);
        for (int i = 1; i < indicesGroup.length; i++) {
            final String[] indices = indicesGroup[i].split(INDEX_SEPARATOR);
            positions.add(Integer.parseInt(indices[0]) - 1);
            if (indices.length > 1 && !indices[1].isEmpty()) {
                textureCoords.add(Integer.parseInt(indices[1]) - 1);
            }
            if (indices.length > 2) {
                normals.add(Integer.parseInt(indices[2]) - 1);
            }
        }
    }

    /**
     * An exception throw by the {@link ObjFileLoader} during loading if any errors are encountered.
     */
    public static class MalformedObjFileException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new exception from the line at which the error occurred and the cause. If the error did not occur on a line, the variable can be passed as null.
         *
         * @param line The line of origin, or null, if not on a line
         * @param cause The original exception
         */
        public MalformedObjFileException(String line, Throwable cause) {
            super(line != null ? "for line \"" + line + "\"" : null, cause);
        }
    }
}
