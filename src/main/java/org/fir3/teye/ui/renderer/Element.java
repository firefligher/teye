package org.fir3.teye.ui.renderer;

/**
 * The primitive rectangle that each {@link Renderer} implementation must be
 * able to render.
 */
public interface Element {
    /**
     * Sets the x-coordinate of the rectangle.
     *
     * @param x The x-coordinate of the rectangle.
     * @throws IllegalArgumentException If <code>x</code> is negative.
     */
    void setX(int x);

    /**
     * Returns the x-coordinate of the rectangle.
     *
     * @return The x-coordinate of the rectangle.
     */
    int getX();

    /**
     * Sets the y-coordinate of the rectangle.
     *
     * @param y The y-coordinate of the rectangle.
     * @throws IllegalArgumentException If <code>y</code> is negative.
     */
    void setY(int y);

    /**
     * Returns the y-coordinate of the rectangle.
     *
     * @return The y-coordinate of the rectangle.
     */
    int getY();

    /**
     * Sets the width of the rectangle.
     *
     * @param width The width of the rectangle.
     * @throws IllegalArgumentException If <code>width</code> is negative.
     */
    void setWidth(int width);

    /**
     * Returns the width of the rectangle.
     *
     * @return The width of the rectangle.
     */
    int getWidth();

    /**
     * Sets the height of the rectangle.
     *
     * @param height    The height of the rectangle.
     * @throws IllegalArgumentException If <code>height</code> is negative.
     */
    void setHeight(int height);

    /**
     * Returns the height of the rectangle.
     *
     * @return The height of the rectangle.
     */
    int getHeight();

    /**
     * Sets the red component of the rectangle's fill color.
     *
     * @param red   The red component of the rectangle's fill color.
     *
     * @throws IllegalArgumentException If <code>red</code> is less than zero
     *                                  or greater than 255.
     */
    void setRed(int red);

    /**
     * Return the red component of the rectangle's fill color.
     *
     * @return  The red component of the rectangle's fill color.
     */
    int getRed();

    /**
     * Sets the green component of the rectangle's fill color.
     *
     * @param green The green component of the rectangle's fill color.
     *
     * @throws IllegalArgumentException If <code>green</code> is less than zero
     *                                  or greater than 255.
     */
    void setGreen(int green);

    /**
     * Return the green component of the rectangle's fill color.
     *
     * @return  The green component of the rectangle's fill color.
     */
    int getGreen();

    /**
     * Sets the blue component of the rectangle's fill color.
     *
     * @param blue  The blue component of the rectangle's fill color.
     *
     * @throws IllegalArgumentException If <code>blue</code> is less than zero
     *                                  or greater than 255.
     */
    void setBlue(int blue);

    /**
     * Return the blue component of the rectangle's fill color.
     *
     * @return  The blue component of the rectangle's fill color.
     */
    int getBlue();

    /**
     * Sets the alpha component of the rectangle's fill color.
     *
     * @param alpha The alpha component of the rectangle's fill color.
     *
     * @throws IllegalArgumentException If <code>alpha</code> is less than zero
     *                                  or greater than 255.
     */
    void setAlpha(int alpha);

    /**
     * Return the alpha component of the rectangle's fill color.
     *
     * @return  The alpha component of the rectangle's fill color.
     */
    int getAlpha();

    /**
     * Sets the texture of the rectangle.
     *
     * @param texture   The texture of the rectangle, may be <code>null</code>,
     *                  if you do not wish to texturize the rectangle.
     *
     * @throws IllegalArgumentException If the passed <code>texture</code> is
     *                                  unknown to the {@link Element}'s
     *                                  {@link Renderer}.
     */
    void setTexture(Texture texture);

    /**
     * Returns the texture of the rectangle.
     *
     * @return  Either the instance of the rectangle's texture or
     *          <code>null</code>, if the rectangle is not
     *          texturized.
     */
    Texture getTexture();

    /**
     * Sets the x-coordinate of the rectangle's texture.
     *
     * @param x The x-coordinate of the rectangle's texture.
     *
     * @throws IllegalArgumentException If <code>x</code> is less than zero.
     */
    void setTextureX(int x);

    /**
     * Returns the x-coordinate of the rectangle's texture.
     *
     * @return  The x-coordinate of the rectangle's texture.
     */
    int getTextureX();

    /**
     * Sets the y-coordinate of the rectangle's texture.
     *
     * @param y The y-coordinate of the rectangle's texture.
     *
     * @throws IllegalArgumentException If <code>y</code> is less than zero.
     */
    void setTextureY(int y);

    /**
     * Returns the y-coordinate of the rectangle's texture.
     *
     * @return  The y-coordinate of the rectangle's texture.
     */
    int getTextureY();

    /**
     * Sets the width of the rectangle's texture.
     *
     * @param width The width of the rectangle's texture.
     *
     * @throws IllegalArgumentException If <code>width</code> is less than
     *                                  zero.
     */
    void setTextureWidth(int width);

    /**
     * Returns the width of the rectangle's texture.
     *
     * @return  The width of the rectangle's texture.
     */
    int getTextureWidth();

    /**
     * Sets the height of the rectangle's texture.
     *
     * @param height    The height of the rectangle's texture.
     *
     * @throws IllegalArgumentException If <code>height</code> is less than
     *                                  zero.
     */
    void setTextureHeight(int height);

    /**
     * Returns the height of the rectangle's texture.
     *
     * @return  The height of the rectangle's texture.
     */
    int getTextureHeight();

    /**
     * Sets the z-index of the rectangle.
     *
     * @param zIndex    The z-index of the rectangle, may also be negative.
     */
    void setZIndex(int zIndex);

    /**
     * Returns the z-index of the rectangle.
     *
     * @return  The z-index.
     */
    int getZIndex();
}
