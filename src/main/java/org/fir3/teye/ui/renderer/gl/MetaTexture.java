package org.fir3.teye.ui.renderer.gl;

import lombok.Getter;
import org.fir3.teye.ui.renderer.ColorModel;
import org.fir3.teye.util.PowerOfTwo;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

/**
 * Wrapper of an OpenGL texture.
 */
class MetaTexture implements Disposable {
    /**
     * Returns the corresponding OpenGL pixel format identifier for the passed
     * <code>pixelFormat</code>.
     *
     * @param pixelFormat   The pixel format whose OpenGL identifier shall be
     *                      determined.
     *
     * @return  The OpenGL pixel format identifier or <code>-1</code>, if the
     *          passed <code>pixelFormat</code> is not supported by OpenGL.
     */
    private static int getGLPixelFormat(ColorModel pixelFormat) {
        if (pixelFormat == ColorModel.RGBA_8888) {
            return GL11.GL_RGBA;
        }

        return -1;
    }

    /**
     * Returns the corresponding OpenGL pixel type identifier for the passed
     * <code>pixelFormat</code>.
     *
     * @param pixelFormat   The pixel format whose OpenGL type identifier shall
     *                      be determined.
     *
     * @return  The OpenGL pixel format type or <code>-1</code>, if the passed
     *          <code>pixelFormat</code> is not supported by OpenGL.
     */
    private static int getGLPixelType(ColorModel pixelFormat) {
        if (pixelFormat == ColorModel.RGBA_8888) {
            return GL11.GL_UNSIGNED_BYTE;
        }

        return -1;
    }

    @Getter
    private final int width;

    @Getter
    private final int height;

    @Getter
    private final ColorModel pixelFormat;

    private final int glPixelFormat;
    private final int glPixelType;

    /**
     * The identifier of the OpenGL texture.
     *
     * The value of this attribute may be zero, if there is no OpenGL peer for
     * this instance.
     */
    private int textureId;

    /**
     * Creates a new instance.
     *
     * @param width         The width of the texture.
     * @param height        The height of the texture.
     * @param pixelFormat   The pixel format of the texture.
     *
     * @throws IllegalArgumentException If <code>width</code> or
     *                                  <code>height</code> are less than one
     *                                  or no power of two.
     *
     * @throws NullPointerException     If <code>pixelFormat</code> is
     *                                  <code>null</code>.
     */
    MetaTexture(int width, int height, ColorModel pixelFormat) {
        if (width < 1 || !PowerOfTwo.isPowerOfTwo(width))
            throw new IllegalArgumentException("Invalid width!");

        if (height < 1 || !PowerOfTwo.isPowerOfTwo(height))
            throw new IllegalArgumentException("Invalid height!");

        if (pixelFormat == null)
            throw new NullPointerException("pixelFormat is null!");

        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.glPixelFormat = MetaTexture.getGLPixelFormat(pixelFormat);
        this.glPixelType = MetaTexture.getGLPixelType(pixelFormat);

        if (this.glPixelFormat == -1 || this.glPixelType == -1)
            throw new IllegalArgumentException("Unsupported pixelFormat!");
    }

    @Override
    public void dispose() {
        if (this.textureId < 1)
            return;

        GL11.glDeleteTextures(this.textureId);
        this.textureId = 0;
    }

    /**
     * Initializes this texture instance and prepares the required OpenGL
     * resources.
     *
     * This method must be called from the OpenGL context thread only.
     *
     * @return  Either <code>true</code>, if the initialization succeeds,
     *          otherwise <code>false</code>.
     *
     * @throws IllegalStateException    If this instance has been initialized
     *                                  already.
     */
    boolean initialize() {
        if (this.textureId > 0)
            throw new IllegalStateException("Already initialized!");

        // Clearing the OpenGL error value.
        //
        // NOTE:    Since we do not know which GL operations took place in the
        //          past, we cannot handle any error value here.

        GL11.glGetError();

        // Creating the OpenGL texture peer

        this.textureId = GL11.glGenTextures();

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            return false;

        // Allocating the required memory.

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_NEAREST);

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_NEAREST);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                this.glPixelFormat,
                this.width, this.height,
                0,
                this.glPixelFormat, this.glPixelType,
                0);

        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            // If an error occurred because we attempted to allocate some
            // memory, we need to delete the generated texture identifier to
            // avoid memory leaks.

            GL11.glDeleteTextures(this.textureId);
            this.textureId = 0;
            return false;
        }

        return true;
    }

    /**
     * Returns whether this instance has been initialized or not.
     *
     * @return  Either <code>true</code>, if this texture instance has been
     *          initialized, otherwise <code>false</code>.
     */
    boolean isInitialized() {
        return this.textureId > 0;
    }

    /**
     * Updates the OpenGL texture peer at the specified region with the
     * specified <code>data</code>.
     *
     * @param x         The x-coordinate of the updated region
     * @param y         The y-coordinate of the updated region
     * @param width     The width of the updated region
     * @param height    The height of the updated region
     * @param data      The new data
     *
     * @throws IllegalStateException    If there is no corresponding OpenGL
     *                                  peer.
     *
     * @throws IllegalArgumentException If <code>x</code> or <code>y</code> is
     *                                  negative, if <code>width</code> or
     *                                  <code>height</code> is less than one,
     *                                  if the specified rectangle exceeds the
     *                                  boundaries of this texture, or if
     *                                  <code>data</code> does not have the
     *                                  right number of remaining bytes.
     *
     * @throws NullPointerException     If <code>data</code> is
     *                                  <code>null</code>.
     */
    void update(int x, int y, int width, int height, ByteBuffer data) {
        this.requirePeer();

        // Parameter validation

        if (x < 0)
            throw new IllegalArgumentException("Invalid x!");

        if (y < 0)
            throw new IllegalArgumentException("Invalid y!");

        if (width < 1)
            throw new IllegalArgumentException("Invalid width!");

        if (height < 1)
            throw new IllegalArgumentException("Invalid height!");

        if (x + width > this.width)
            throw new IllegalArgumentException("Invalid x-width-combination!");

        if (y + height > this.height)
            throw new IllegalArgumentException(
                    "Invalid y-height-combination!");

        if (data == null)
            throw new NullPointerException("data is null");

        int requiredBufferSize = this.pixelFormat.computeBufferSize(
                width, height);

        if (data.remaining() != requiredBufferSize)
            throw new IllegalArgumentException(
                    "data does not match width/height!");

        // Clearing the OpenGL error value.
        //
        // NOTE:    Since we do not know what happened inside the OpenGL
        //          context before calling this method, we cannot handle the
        //          returned error code in any way.

        GL11.glGetError();

        // Data upload to GRAM

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);

        if (x == 0 && y == 0 && width == this.width && height == this.height)
            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    this.glPixelFormat,
                    this.width, this.height,
                    0,
                    this.glPixelFormat, this.glPixelType,
                    data);
        else
            GL11.glTexSubImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    x, y,
                    width, height,
                    this.glPixelFormat, this.glPixelType,
                    data);

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new IllegalStateException("Texture upload failed!");
    }

    /**
     * Reads the pixel data of the specified area from the OpenGL texture peer.
     *
     * @param x         The x-coordinate of the rectangle
     * @param y         The y-coordinate of the rectangle
     * @param width     The width of the rectangle
     * @param height    The height of the rectangle
     * @param data      The destination of the rectangle's pixel data
     *
     * @throws IllegalArgumentException If <code>x</code> or <code>y</code> is
     *                                  bellow zero, <code>width</code> or
     *                                  <code>height</code> is bellow one, if
     *                                  the specified rectangle exceeds the
     *                                  size of this texture, or if
     *                                  <code>data</code> is too small to store
     *                                  the requested rectangle.
     *
     * @throws NullPointerException     If <code>data</code> is
     *                                  <code>null</code>.
     */
    void read(int x, int y, int width, int height, ByteBuffer data) {
        this.requirePeer();

        // Parameter validation

        if (x < 0)      throw new IllegalArgumentException("Invalid x!");
        if (y < 0)      throw new IllegalArgumentException("Invalid y!");
        if (width < 1)  throw new IllegalArgumentException("Invalid width!");
        if (height < 1) throw new IllegalArgumentException("Invalid height!");

        if ((x + width) > this.width)
            throw new IllegalArgumentException("Invalid x-width-combination!");

        if ((y + height) > this.height)
            throw new IllegalArgumentException(
                    "Invalid y-height-combination!");

        if (data == null)
            throw new NullPointerException("data is null!");

        int requiredBufferSize = this.pixelFormat.computeBufferSize(
                width, height);

        if (data.remaining() < requiredBufferSize)
            throw new IllegalArgumentException("data is too short!");

        // Resetting the OpenGL error value

        GL11.glGetError();

        // Copying the pixel data
        //
        // NOTE:    Since OpenGL did not support reading a sub-region from a
        //          texture before OpenGL 4.5 (and we also want to support the
        //          previous versions), we need to read the whole texture from
        //          the GRAM and perform the area extraction manually.

        boolean requiresExtraction = (x != 0) ||
                (y != 0) ||
                (width != this.width) ||
                (height != this.height);

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);

        // TODO:    A ByteBuffer cache would be nice to optimize the number of
        //          syscalls (and thus context switches) that are required for
        //          memory allocation.

        ByteBuffer textureBuffer = data;

        if (requiresExtraction)
            textureBuffer = BufferUtils.createByteBuffer(
                    this.pixelFormat.computeBufferSize(
                            this.width, this.height));

        GL11.glGetTexImage(
                GL11.GL_TEXTURE_2D,
                0,
                this.glPixelFormat, this.glPixelType,
                textureBuffer);

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new IllegalStateException(
                    "Failed reading texture data from OpenGL!");

        // Area extraction, if required

        if (!requiresExtraction) {
            return;
        }

        Util.copyRectangle(
                textureBuffer,
                x, y,
                width, height,
                data,
                0, 0,
                this.pixelFormat,
                this.width, width);

        data.flip();
    }

    /**
     * Binds this texture to the currently enabled OpenGL texture unit.
     */
    void bind() {
        this.requirePeer();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
    }

    private void requirePeer() {
        if (this.textureId > 0)
            return;

        throw new IllegalStateException("No peer!");
    }
}
