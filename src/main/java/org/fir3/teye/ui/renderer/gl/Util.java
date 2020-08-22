package org.fir3.teye.ui.renderer.gl;

import org.fir3.teye.ui.renderer.ColorModel;

import java.nio.ByteBuffer;

final class Util {
    /**
     * Copies the pixels of the specified rectangle from <code>src</code> to
     * <code>dst</code>.
     *
     * @param src           The source buffer.
     * @param x             The x-coordinate of the rectangle in the
     *                      <code>src</code> buffer.
     *
     * @param y             The y-coordinate of the rectangle in the
     *                      <code>src</code> buffer.
     *
     * @param width         The width of the rectangle.
     * @param height        The height of the rectangle.
     * @param dst           The destination buffer.
     * @param dstX          The x-offset of the copied rectangle inside
     *                      <code>dst</code>.
     *
     * @param dstY          The y-offset of the copied rectangle inside
     *                      <code>dst</code>.
     *
     * @param pixelFormat   The format of the copied pixel data.
     * @param srcWidth      The width of the <code>src</code> buffer's complete
     *                      rectangle.
     *
     * @param dstWidth      The width of the <code>dst</code> buffer's complete
     *                      rectangle.
     *
     * @throws NullPointerException     If either <code>src</code>,
     *                                  <code>dst</code> or
     *                                  <code>pixelFormat</code> is
     *                                  <code>null</code>.
     *
     * @throws IllegalArgumentException If <code>src</code> or <code>dst</code>
     *                                  has a too small limit, or
     *                                  <code>x</code> or <code>y</code> or
     *                                  <code>dstX</code> or <code>dstY</code>
     *                                  is less than one, or if
     *                                  <code>width</code> or
     *                                  <code>height</code> or
     *                                  <code>srcWidth</code> or
     *                                  <code>dstWidth</code> is less than one.
     */
    static void copyRectangle(
            ByteBuffer src,
            int x, int y,
            int width, int height,
            ByteBuffer dst,
            int dstX, int dstY,
            ColorModel pixelFormat,
            int srcWidth,
            int dstWidth) {
        // Argument validation

        if (src == null)    throw new NullPointerException("src is null!");
        if (dst == null)    throw new NullPointerException("dst is null!");

        if (pixelFormat == null)
            throw new NullPointerException("pixelFormat is null!");

        if (x < 0)  throw new IllegalArgumentException("x less than zero!");
        if (y < 0)  throw new IllegalArgumentException("y less than zero!");

        if (dstX < 0)
            throw new IllegalArgumentException("dstX less than zero!");

        if (dstY < 0)
            throw new IllegalArgumentException("dstY less than zero!");

        if (width < 1)
            throw new IllegalArgumentException("width less than one!");

        if (height < 1)
            throw new IllegalArgumentException("height less than one!");

        if (srcWidth < 1)
            throw new IllegalArgumentException("srcWidth less than one!");

        if (dstWidth < 1)
            throw new IllegalArgumentException("dstWidth less than one!");

        // Capacity validation

        int requiredSrcCapacity = pixelFormat.computeBufferSize(
                x + width, y + width);

        int requiredDstCapacity = pixelFormat.computeBufferSize(
                dstX + width, dstY + height);

        if (requiredSrcCapacity > src.limit())
            throw new IllegalArgumentException("src limit too low!");

        if (requiredDstCapacity > dst.limit())
            throw new IllegalArgumentException("dst limit too low!");

        // Copying pixel data
        //
        // TODO:    The following loops may be optimized: We can copy whole
        //          rows of pixels (same y, continuous x) at once.

        int pixelSize = pixelFormat.getBytesPerPixel();
        byte[] pixel = new byte[pixelSize];

        for (int offY = 0; offY < height; offY++) {
            for (int offX = 0; offX < width; offX++) {
                int srcPos =
                        (((y + offY) * srcWidth) + (x + offX)) * pixelSize;

                int dstPos =
                        (((dstY + offY) * dstWidth) + (x + offX)) * pixelSize;

                src.position(srcPos);
                src.get(pixel);

                dst.position(dstPos);
                dst.put(pixel);
            }
        }
    }

    private Util() {}
}
