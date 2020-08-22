package org.fir3.teye.ui.renderer;

import java.nio.ByteBuffer;

final class TestUtil {
    static final int DEFAULT_ATLAS_WIDTH = 1024;
    static final int DEFAULT_ATLAS_HEIGHT = 1024;
    static final int DEFAULT_FRAGMENT_WIDTH = 120;
    static final int DEFAULT_FRAGMENT_HEIGHT = 68;
    static final ColorModel DEFAULT_FRAGMENT_PIXEL_FORMAT =
            ColorModel.RGBA_8888;

    static ByteBuffer createTextureBuffer(
            int width, int height,
            ColorModel pixelFormat) {
        return ByteBuffer.allocate(
                width * height * pixelFormat.getBytesPerPixel());
    }

    static DummyTexture createDummyTexture(int width, int height) {
        ColorModel pixelFormat = TestUtil.DEFAULT_FRAGMENT_PIXEL_FORMAT;
        ByteBuffer buffer = TestUtil.createTextureBuffer(
                width, height,
                pixelFormat);

        return new DummyTexture(width, height, pixelFormat, buffer);
    }

    static DummyTexture createDummyTexture() {
        return TestUtil.createDummyTexture(
                TestUtil.DEFAULT_FRAGMENT_WIDTH,
                TestUtil.DEFAULT_FRAGMENT_HEIGHT);
    }

    static DummyTextureAtlas createDummyAtlas(int width, int height) {
        return new DummyTextureAtlas(width, height);
    }

    static DummyTextureAtlas createDummyAtlas() {
        return TestUtil.createDummyAtlas(
                TestUtil.DEFAULT_ATLAS_WIDTH,
                TestUtil.DEFAULT_ATLAS_HEIGHT);
    }

    static DummyRenderer createDummyRenderer() {
        return new DummyRenderer();
    }

    static DummyRenderer createDummyRenderer(float oversizeFactor) {
        return new DummyRenderer(oversizeFactor);
    }

    private TestUtil() {}
}
