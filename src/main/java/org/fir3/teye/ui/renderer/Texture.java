package org.fir3.teye.ui.renderer;

/**
 * A texture that can be used to texturize a {@link Element}.
 */
public interface Texture {
    /**
     * Returns the width of the texture.
     *
     * @return  The texture's width
     */
    int getWidth();

    /**
     * Returns the height of the texture.
     *
     * @return  The texture's height
     */
    int getHeight();
}
