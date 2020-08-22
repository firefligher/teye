package org.fir3.teye.ui.renderer;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractTextureTest {
    @Test
    public void testConstructorArgumentValidation() {
        // Width < 0

        assertThrows(IllegalArgumentException.class, () -> {
            int width = 0;
            int height = TestUtil.DEFAULT_FRAGMENT_HEIGHT;
            ColorModel pixelFormat = TestUtil.DEFAULT_FRAGMENT_PIXEL_FORMAT;
            ByteBuffer buffer = TestUtil.createTextureBuffer(
                    width, height,
                    pixelFormat);

            new DummyTexture(width, height, pixelFormat, buffer);
        });

        // Height < 0

        assertThrows(IllegalArgumentException.class, () -> {
            int width = TestUtil.DEFAULT_FRAGMENT_WIDTH;
            int height = 0;
            ColorModel pixelFormat = TestUtil.DEFAULT_FRAGMENT_PIXEL_FORMAT;
            ByteBuffer buffer = TestUtil.createTextureBuffer(
                    width, height,
                    pixelFormat);

            new DummyTexture(width, height, pixelFormat, buffer);
        });

        // pixelFormat = null

        assertThrows(NullPointerException.class, () -> {
            int width = TestUtil.DEFAULT_FRAGMENT_WIDTH;
            int height = TestUtil.DEFAULT_FRAGMENT_HEIGHT;
            ColorModel pixelFormat = null;
            ByteBuffer buffer = ByteBuffer.allocate(width * height);

            new DummyTexture(width, height, pixelFormat, buffer);
        });

        // data = null

        assertThrows(NullPointerException.class, () -> {
            int width = TestUtil.DEFAULT_FRAGMENT_WIDTH;
            int height = TestUtil.DEFAULT_FRAGMENT_HEIGHT;
            ColorModel pixelFormat = TestUtil.DEFAULT_FRAGMENT_PIXEL_FORMAT;
            ByteBuffer buffer = null;

            new DummyTexture(width, height, pixelFormat, buffer);
        });

        // data has an invalid size

        assertThrows(IllegalArgumentException.class, () -> {
            int width = TestUtil.DEFAULT_FRAGMENT_WIDTH;
            int height = TestUtil.DEFAULT_FRAGMENT_HEIGHT;
            ColorModel pixelFormat = TestUtil.DEFAULT_FRAGMENT_PIXEL_FORMAT;
            ByteBuffer buffer = ByteBuffer.allocate(1);

            new DummyTexture(width, height, pixelFormat, buffer);
        });
    }
}
