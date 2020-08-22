package org.fir3.teye.ui.renderer;

/**
 * Represents a super texture that contains multiple smaller {@link Texture}
 * instances.
 *
 * @param <T>   The type of a fragment of the atlas
 */
public interface TextureAtlas<T extends Texture> {
    /**
     * Returns whether the specified <code>fragment</code> is part of this
     * atlas or not.
     *
     * @param fragment  The fragment that is looked for
     *
     * @return  Either <code>true</code>, if the specified
     *          <code>fragment</code> is part of this atlas, otherwise
     *          <code>false</code>.
     */
    boolean contains(T fragment);

    /**
     * Returns the x-coordinate of the specified <code>fragment</code>.
     *
     * @param fragment  The x-coordinate of the specified <code>fragment</code>
     *                  inside this atlas.
     *
     * @return  Either greater or equal to <code>0</code>, if the
     *          <code>fragment</code> is part of this atlas, otherwise
     *          <code>-1</code>.
     */
    int getX(T fragment);

    /**
     * Returns the y-coordinate of the specified <code>fragment</code>.
     *
     * @param fragment  The y-coordinate of the specified <code>fragment</code>
     *                  inside this atlas.
     *
     * @return  Either greater or equal to <code>0</code>, if the
     *          <code>fragment</code> is part of this atlas, otherwise
     *          <code>-1</code>.
     */
    int getY(T fragment);

    /**
     * Attempts to insert the specified <code>fragment</code> into this atlas.
     *
     * If the specified <code>fragment</code> is already part of this atlas,
     * calling this method will have no effect and will always return
     * successfully.
     *
     * NOTE:    Nobody else but the corresponding {@link BaseTextureManager}
     *          implementation should call this method.
     *
     * @param fragment  The fragment that is being inserted
     *
     * @return  Either <code>true</code>, if the attempt succeeded, otherwise
     *          <code>false</code>.
     */
    boolean insert(T fragment);

    /**
     * Marks the area of the atlas that is occupied by the specified
     * <code>fragment</code> as unoccupied.
     *
     * If the specified <code>fragment</code> is not part of this atlas,
     * calling this method has no effect.
     *
     * NOTE:    Nobody else but the corresponding {@link BaseTextureManager}
     *          implementation should call this method.
     *
     * @param fragment  The fragment whose space shall be marked as
     *                  unoccupied.
     */
    void free(T fragment);

    /**
     * Returns one minus the ratio of the number of pixels of the largest
     * unoccupied rectangle and the total number of pixels of this atlas
     * instance.
     *
     * @return  A value between 0.0 and 1.0 (both inclusive), that is one minus
     *          the ratio of the largest unoccupied rectangle compared to the
     *          atlas' size.
     */
    float getOccupation();

    /**
     * Computes the border ratio of the provided rectangle.
     *
     * This method looks for the smallest unoccupied rectangle of the specified
     * <code>width</code> and <code>height</code> inside this atlas and
     * calculates the number of unoccupied pixels that may become unusable by
     * inserting a {@link Texture} fragment of the specified size. The ratio of
     * the specified rectangle and the potentially unusable pixels is the
     * returned value.
     *
     * @param width     The width of the required rectangle.
     * @param height    The height of the required rectangle.
     *
     * @return  Either the ratio of rectangle pixels vs. border pixels, or a
     *          negative value, if the specified rectangle does not fit into
     *          this atlas instance.
     */
    float computeBorderRatio(int width, int height);
}
