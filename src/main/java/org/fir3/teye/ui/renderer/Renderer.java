package org.fir3.teye.ui.renderer;

import org.fir3.teye.ui.Modifiable;
import org.fir3.teye.ui.Modification;

import java.nio.ByteBuffer;

/**
 * The specification of a renderer that is capable of rendering
 * {@link Element}s.
 */
public interface Renderer<T extends Modification>
        extends Modifiable<Renderer<T>, T> {
    /**
     * Initializes the renderer.
     */
    void initialize();

    /**
     * Destroys the renderer.
     *
     * This operation will destroy all created {@link Element} and
     * {@link Texture} instances automatically.
     */
    void destroy();

    /**
     * Renders the current scene.
     */
    void render();

    /**
     * Returns a new element that may be used for rendering.
     *
     * The caller is required to release the returned {@link Element} instance
     * by calling {@link #release(Element)} manually in the future.
     *
     * @return  The new element
     */
    Element newElement();

    /**
     * Tells this {@link Renderer} that the specified <code>element</code> is
     * no longer in use.
     *
     * @param element   The element that has been acquired by a previous call
     *                  of {@link #newElement()} (same {@link Renderer}
     *                  instance).
     *
     * @throws IllegalArgumentException If the passed <code>element</code> is
     *                                  unknown to this instance.
     *
     * @throws NullPointerException     If the passed <code>element</code> is
     *                                  <code>null</code>.
     */
    void release(Element element);

    /**
     * Makes the passed image available as {@link Texture}, this is required to
     * texturize a {@link Element} with the image.
     *
     * @param width         The width of the image
     * @param height        The height of the image
     * @param data          The pixel data of the the image
     * @param pixelFormat   The pixel format of the image
     * @return  The corresponding texture instance.
     *
     * @throws IllegalArgumentException If either <code>width</code> or
     *                                  <code>height</code> or both are less
     *                                  than zero, or if the passed
     *                                  <code>data</code> buffer does not match
     *                                  the remaining parameters.
     *
     * @throws NullPointerException     If the the passed <code>data</code> or
     *                                  <code>pixelFormat</code> or both are
     *                                  <code>null</code>.
     */
    Texture newTexture(
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat);

    /**
     * Tells this {@link Renderer} that the passed <code>texture</code> is no
     * longer in use.
     *
     * @param texture   The texture that is no longer in use.
     *
     * @throws IllegalArgumentException If the passed <code>texture</code> is
     *                                  unknown to this {@link Renderer}
     *                                  instance.
     *
     * @throws NullPointerException     If the passed <code>texture</code> is
     *                                  <code>null</code>.
     */
    void release(Texture texture);
}
