package org.fir3.teye.ui.renderer;

import java.nio.ByteBuffer;

final class DummyTexture extends AbstractTexture {
    DummyTexture(
            int width, int height,
            ColorModel pixelFormat,
            ByteBuffer data) {
        super(width, height, pixelFormat, data);
    }
}
