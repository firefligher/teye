package org.fir3.teye.ui.renderer;

import java.nio.ByteBuffer;

final class DummyTextureAtlas extends AbstractTextureAtlas<DummyTexture> {
    private static void simulateAccess(
            int width, int height,
            ColorModel pixelFormat,
            ByteBuffer buffer) {
        int bytes = width * height * pixelFormat.getBytesPerPixel();
        buffer.position(buffer.position() + bytes);
    }

    DummyTextureAtlas(int width, int height) {
        super(width, height);
    }

    @Override
    protected void insert(
            int x, int y,
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat) {
        // Simulating reading from the passed data ByteBuffer

        DummyTextureAtlas.simulateAccess(width, height, pixelFormat, data);
    }

    @Override
    protected void copy(
            int x, int y,
            int width, int height,
            ColorModel dstPixelFormat,
            ByteBuffer dst) {
        // We need to increment the position attribute of the ByteBuffer to
        // simulate writing

        DummyTextureAtlas.simulateAccess(width, height, dstPixelFormat, dst);
    }
}
