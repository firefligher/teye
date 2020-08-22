package org.fir3.teye.ui.renderer.gl;

import org.fir3.teye.ui.renderer.AbstractTexture;
import org.fir3.teye.ui.renderer.ColorModel;

import java.nio.ByteBuffer;

final class GLTexture extends AbstractTexture {
    GLTexture(int width, int height, ColorModel pixelFormat, ByteBuffer data) {
        super(width, height, pixelFormat, data);
    }
}
