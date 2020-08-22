package org.fir3.teye.ui.renderer.gl;

import org.fir3.teye.ui.renderer.ColorModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MetaTextureTest {
    @BeforeEach
    public void createContext() {
        TestUtil.createContext();
    }

    @AfterEach
    public void destroyContext() {
        TestUtil.destroyContext();
    }

    @Test
    public void testConstructorParameterValidation() {
        // Width too small

        assertThrows(IllegalArgumentException.class,
                () -> new MetaTexture(0, 1024, ColorModel.RGBA_8888));

        // Height too small

        assertThrows(IllegalArgumentException.class,
                () -> new MetaTexture(1024, 0, ColorModel.RGBA_8888));

        // Null pixel format

        assertThrows(NullPointerException.class,
                () -> new MetaTexture(1024, 1024, null));

        // Width not a power of two

        assertThrows(IllegalArgumentException.class,
                () -> new MetaTexture(1000, 1024, ColorModel.RGBA_8888));

        // Height not a power of two

        assertThrows(IllegalArgumentException.class,
                () -> new MetaTexture(1024, 1000, ColorModel.RGBA_8888));
    }

    @Test
    public void testInitializeAndDispose() {
        MetaTexture texture = new MetaTexture(
                1024, 1024,
                ColorModel.RGBA_8888);

        // Since nothing has been initialized, texture should not be
        // initialized

        assertFalse(texture.isInitialized());

        // After initializing the texture, it should be initialized

        assertTrue(texture.initialize());
        assertTrue(texture.isInitialized());

        // After disposing the texture it should not initialized anymore

        texture.dispose();
        assertFalse(texture.isInitialized());
    }

    @Test
    public void testUpdateAndRead() {
        MetaTexture texture = new MetaTexture(
                1024, 1024,
                ColorModel.RGBA_8888);

        ByteBuffer imgRand = TestUtil.readImageRGBA8888(
                TestUtil.PATH_IMG_RAND);

        int imgRandWidth = TestUtil.WIDTH_IMG_RAND;
        int imgRandHeight = TestUtil.HEIGHT_IMG_RAND;

        // Reading the test image to a byte array and rewinding the buffer

        byte[] imgRandBytes = new byte[imgRand.limit()];
        imgRand.get(imgRandBytes);
        imgRand.rewind();

        // Initializing the texture

        assertTrue(texture.initialize());

        // Uploading the image

        texture.update(0, 0, imgRandWidth, imgRandHeight, imgRand);

        // Downloading the image

        ByteBuffer downloadedImg = BufferUtils.createByteBuffer(
                imgRandWidth * imgRandHeight * 4);

        texture.read(0, 0, imgRandWidth, imgRandHeight, downloadedImg);

        // Equality check

        byte[] downloadedImgBytes = new byte[downloadedImg.limit()];
        downloadedImg.get(downloadedImgBytes);

        assertArrayEquals(imgRandBytes, downloadedImgBytes);
    }
}
