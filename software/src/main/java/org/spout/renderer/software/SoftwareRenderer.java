/*
 * This file is part of Caustic Software.
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 * Caustic Software is licensed under the Spout License Version 1.
 *
 * Caustic Software is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Caustic Software is distributed in the hope that it will be useful, but WITHOUT ANY
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
package org.spout.renderer.software;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import org.spout.renderer.api.util.Rectangle;

/**
 *
 */
public class SoftwareRenderer extends Canvas {
    private int width = 100, height = 100;
    private int scale = 4;
    private BufferedImage image;
    private final Rectangle viewPort = new Rectangle(width, height);
    private int[] pixels;
    private SoftwareProgram program;

    SoftwareRenderer() {
    }

    Rectangle getViewPort() {
        return viewPort;
    }

    void setViewPort(Rectangle viewPort) {
        this.viewPort.set(viewPort);
    }

    SoftwareProgram getProgram() {
        return program;
    }

    void setProgram(SoftwareProgram program) {
        this.program = program;
    }

    void init() {
        final Dimension size = new Dimension(width * scale, height * scale);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        final JFrame frame = new JFrame("Test");
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(this, BorderLayout.CENTER);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        createBufferStrategy(3);
    }

    void render() {
        final BufferStrategy bufferStrategy = getBufferStrategy();
        final Graphics graphics = bufferStrategy.getDrawGraphics();
        //graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.drawImage(image, 0, 0, width * scale, height * scale, null);
        graphics.dispose();
        bufferStrategy.show();
    }

    void clearPixels() {
        Arrays.fill(pixels, 0);
    }

    void writePixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= width) {
            throw new IllegalArgumentException("(" + x + ", " + y + ") not within (0, 0) to (" + (width - 1) + ", " + (height - 1) + ")");
        }
        pixels[x + y * width] = color;
    }
}
