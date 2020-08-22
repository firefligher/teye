package org.fir3.teye.ui.renderer.gl;

import lombok.AccessLevel;
import lombok.Getter;
import org.fir3.teye.ui.renderer.AbstractTextureAtlas;
import org.fir3.teye.ui.renderer.ColorModel;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

/**
 * The OpenGL-specific implementation of the
 * {@link org.fir3.mediashare.server.ui.renderer.TextureAtlas} interface.
 */
final class GLTextureAtlas
        extends AbstractTextureAtlas<GLTexture> {
    private final GLTextureManager textureManager;

    @Getter(AccessLevel.PACKAGE)
    private final ColorModel pixelFormat;

    /**
     * This attribute is being used for storing the pixel data while it is not
     * being stored on the GRAM.
     */
    private ByteBuffer buffer;

    GLTextureAtlas(
            int width, int height,
            GLTextureManager textureManager,
            ColorModel pixelFormat) {
        super(width, height);

        this.textureManager = textureManager;
        this.pixelFormat = pixelFormat;
    }

    @Override
    protected void insert(
            int x, int y,
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat) {
        this.textureManager.bind(this)
                .update(x, y, width, height, data);
    }

    @Override
    protected void copy(
            int x, int y,
            int width, int height,
            ColorModel dstPixelFormat,
            ByteBuffer dst) {
        if (dstPixelFormat != this.pixelFormat)
            throw new IllegalArgumentException("Invalid dstPixelFormat!");

        if (this.buffer != null) {
            Util.copyRectangle(
                    this.buffer,
                    x, y,
                    width, height,
                    dst,
                    0, 0,
                    this.pixelFormat,
                    this.getWidth(), width);

            dst.flip();
            return;
        }

        this.textureManager.bind(this).read(x, y, width, height, dst);
    }

    /**
     * Uploads the whole atlas to the specified <code>target</code> OpenGL
     * texture.
     *
     * @param target    The target OpenGL texture
     */
    void restore(MetaTexture target) {
        if (this.buffer == null)
            return;

        target.update(0,0, this.getWidth(), this.getHeight(), this.buffer);
        this.buffer = null;
    }

    /**
     * Downloads the atlas' data from the specified <code>source</code> OpenGL
     * texture to an internal {@link ByteBuffer}.
     *
     * @param source    The source OpenGL texture
     */
    void save(MetaTexture source) {
        int width = this.getWidth();
        int height = this.getHeight();

        if (this.buffer == null) {
            this.buffer = BufferUtils.createByteBuffer(
                    this.pixelFormat.computeBufferSize(width, height));
        }

        source.read(0, 0, width, height, this.buffer);
    }
}
