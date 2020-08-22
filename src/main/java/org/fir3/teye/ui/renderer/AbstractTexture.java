package org.fir3.teye.ui.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
public abstract class AbstractTexture implements Texture {
    private final int width;
    private final int height;
    private final ColorModel pixelFormat;

    /**
     * The pixel data of this texture.
     *
     * If this texture instance is part of a {@link AbstractTextureAtlas}, this
     * attribute will be <code>null</code>, as the pixel data is stored in the
     * atlas buffer.
     *
     * Only if this instance is not part of a {@link AbstractTextureAtlas}
     * instance, this attribute will be valid.
     *
     * NOTE:    To prevent memory leaks, this attribute should be the only
     *          permanent reference to the corresponding {@link ByteBuffer}
     *          object.
     *          Also, this buffer is not required to be allocated directly.
     */
    private ByteBuffer data;

    /**
     * The x-coordinate of this texture fragment inside its
     * {@link AbstractTextureAtlas} instance.
     *
     * If this texture is not part of a {@link AbstractTextureAtlas}, this is
     * required to be negative.
     */
    private int x;

    /**
     * The y-coordinate of this texture fragment inside its
     * {@link AbstractTextureAtlas} instance.
     *
     * If this texture is not part of a {@link AbstractTextureAtlas}, this is
     * required to be negative.
     */
    private int y;

    /**
     * Creates a new instance.
     *
     * Please note that the passed <code>data</code> {@link ByteBuffer} will be
     * used as it is for backing the new texture instance with the actual pixel
     * data. This means that you should not use it for anything else but this
     * instance.
     *
     * @param width         The width of the texture.
     * @param height        The height of the texture.
     * @param pixelFormat   The encoding of the texture's pixels.
     * @param data          The texture's pixel data.
     *
     * @throws IllegalArgumentException If either <code>width</code> or
     *                                  <code>height</code> are less than one,
     *                                  or if the passed <code>data</code>
     *                                  buffer does not have the right size
     *                                  (greater or smaller).
     *
     * @throws NullPointerException     If either the passed
     *                                  <code>pixelFormat</code> or
     *                                  <code>data</code> is <code>null</code>.
     */
    public AbstractTexture(
            int width, int height,
            ColorModel pixelFormat,
            ByteBuffer data) {
        if (width < 1)
            throw new IllegalArgumentException("Invalid width!");

        if (height < 1)
            throw new IllegalArgumentException("Invalid height!");

        if (pixelFormat == null)
            throw new NullPointerException("pixelFormat is null!");

        if (data == null)
            throw new NullPointerException("data is null!");

        // NOTE:    Since data will be used for backing this texture instance
        //          with the actual texture data, it should contain the exact
        //          number of required bytes. (Anything else does not make a
        //          lot of sense...)

        if (data.remaining() != pixelFormat.computeBufferSize(width, height))
            throw new IllegalArgumentException("Invalid data!");

        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.data = data;
        this.x = -1;
        this.y = -1;
    }
}
