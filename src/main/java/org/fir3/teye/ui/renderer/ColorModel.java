package org.fir3.teye.ui.renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ColorModel {
    RGBA_8888(4);

    /**
     * The number of bytes that this model requires per pixel.
     */
    @Getter
    private final int bytesPerPixel;

    /**
     * Computes the number of bytes that are required for saving an image with
     * the specified size and with the current color model without any
     * compression.
     *
     * @param width     The width of the image
     * @param height    The height of the image
     * @return  The required size for storing the data of the image.
     *
     * @throws IllegalArgumentException If the specified <code>width</code> or
     *                                  <code>height</code> is less than one.
     */
    public int computeBufferSize(int width, int height) {
        if (width < 1)
            throw new IllegalArgumentException("Invalid width!");

        if (height < 1)
            throw new IllegalArgumentException("Invalid height!");

        return width * height * this.bytesPerPixel;
    }
}
