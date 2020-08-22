package org.fir3.teye.ui.renderer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseTextureManager<
        A extends TextureAtlas<T>,
        T extends Texture> {
    private static final int DEFAULT_ATLAS_WIDTH = 1024;
    private static final int DEFAULT_ATLAS_HEIGHT = 1024;
    private static final boolean DEFAULT_ALLOW_OVERSIZED = true;
    private static final float DEFAULT_REARRANGEMENT_THRESHOLD = 0.3F;

    private final Map<T, A> fragmentAssignments;
    private final int defaultAtlasWidth, defaultAtlasHeight;
    private final boolean allowOversized;
    private final float rearrangementThreshold;

    /**
     * Creates a new instance.
     *
     * @param defaultAtlasWidth         The default width of a
     *                                  {@link TextureAtlas} instance.
     *
     * @param defaultAtlasHeight        The default height of a
     *                                  {@link TextureAtlas} instance.
     *
     * @param allowOversized            If this flag is enabled,
     *                                  {@link Texture} instances that exceed
     *                                  the default sizes of a
     *                                  {@link TextureAtlas} will be stored in
     *                                  a oversized {@link TextureAtlas}
     *                                  instance.
     *                                  If disabled, attempting to create an
     *                                  oversized {@link Texture} instance will
     *                                  fail with an exception.
     *
     * @param rearrangementThreshold    It will be attempted to reassign the
     *                                  {@link Texture} fragments of a
     *                                  {@link TextureAtlas} to other existing
     *                                  {@link TextureAtlas} instances, if the
     *                                  returned value of the original
     *                                  {@link TextureAtlas}'
     *                                  {@link TextureAtlas#getOccupation()}
     *                                  method falls below this specified
     *                                  value.
     */
    protected BaseTextureManager(
            int defaultAtlasWidth, int defaultAtlasHeight,
            boolean allowOversized,
            float rearrangementThreshold) {
        this.fragmentAssignments = new HashMap<>();
        this.defaultAtlasWidth = defaultAtlasWidth;
        this.defaultAtlasHeight = defaultAtlasHeight;
        this.allowOversized = allowOversized;
        this.rearrangementThreshold = rearrangementThreshold;
    }

    protected BaseTextureManager() {
        this(BaseTextureManager.DEFAULT_ATLAS_WIDTH,
                BaseTextureManager.DEFAULT_ATLAS_HEIGHT,
                BaseTextureManager.DEFAULT_ALLOW_OVERSIZED,
                BaseTextureManager.DEFAULT_REARRANGEMENT_THRESHOLD);
    }

    /**
     * Returns the {@link TextureAtlas} that contains the specified
     * <code>texture</code>.
     *
     * @param texture   The texture whose {@link TextureAtlas} shall be
     *                  resolved
     *
     * @return  The {@link TextureAtlas} of the specified <code>texture</code>
     *
     * @throws IllegalArgumentException If the specified <code>texture</code>
     *                                  is unknown.
     *
     * @throws NullPointerException     If the value passed as
     *                                  <code>texture</code> is
     *                                  <code>null</code>.
     */
    public A getAtlas(T texture) {
        if (texture == null)
            throw new NullPointerException("texture is null!");

        if (!this.fragmentAssignments.containsKey(texture))
            throw new IllegalArgumentException("texture is unknown!");

        return this.fragmentAssignments.get(texture);
    }

    /**
     * Inserts the fragment data into a {@link TextureAtlas} and makes it
     * available for rendering.
     *
     * @param width         The width of the fragment.
     * @param height        The height of the fragment.
     * @param data          The pixel data of the fragment.
     * @param pixelFormat   The format of the specified <code>data</code>.
     *
     * @return  A reference to the texture fragment.
     *
     * @throws IllegalArgumentException If the specified <code>width</code>
     *                                  and/or <code>height</code> exceeds the
     *                                  default size of a {@link TextureAtlas}
     *                                  and the <code>allowOversized</code> has
     *                                  been disabled, or, if the passed
     *                                  <code>data</code> buffer does not match
     *                                  the remaining parameters.
     *
     * @throws NullPointerException     If either the passed <code>data</code>
     *                                  object and/or the passed
     *                                  <code>pixelFormat</code> is
     *                                  <code>null</code>.
     *
     * @throws IllegalStateException    If the specified sizes of the fragment
     *                                  exceed the limits of the current
     *                                  context.
     */
    public final T createTexture(
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat) {
        // Null-check

        if (data == null)
            throw new NullPointerException("data is null!");

        if (pixelFormat == null)
            throw new NullPointerException("pixelFormat is null!");

        // Consistency check of the parameters.

        int requiredByteCount =
                width * height * pixelFormat.getBytesPerPixel();

        if (requiredByteCount != data.remaining())
            throw new IllegalArgumentException(
                    "Invalid number of bytes in data!");

        // First of all, we should check, if the texture's sizes exceed the
        // default sizes of an atlas. If that's the case, we need to create a
        // custom atlas instance, because none of the existing will fit.

        A targetAtlas = null;

        if (width > this.defaultAtlasWidth ||
                height > this.defaultAtlasHeight) {
            if (this.allowOversized)
                throw new IllegalArgumentException(
                        "width/height exceeds the atlas size!");

            targetAtlas = this.createAtlas(width, height);
        } else {
            // Otherwise, we try to put the new texture into the atlas that
            // contains the unoccupied rectangle that fits the best.
            //
            // Fitting the best: By inserting the texture into an atlas of
            //                   multiple textures we may waste some border
            //                   area between different textures, because their
            //                   sizes do not match perfectly.
            //                   Those border area rectangles are lost most of
            //                   the time because they are too small for the
            //                   most textures.

            Collection<A> atlantes = this.fragmentAssignments.values();
            float targetRatio = Float.MAX_VALUE;

            for (A potentialAtlas : atlantes) {
                float ratio = potentialAtlas.computeBorderRatio(width, height);

                // If the specified texture does not fit into the atlas, we skip
                // to the next.

                if (ratio < 0.0F)
                    continue;

                // If the targetRatio is greater than the ratio of the
                // potentialAtlas, we choose the potentialAtlas as new
                // targetAtlas.

                if (ratio < targetRatio) {
                    targetAtlas = potentialAtlas;
                    targetRatio = ratio;
                }

                // If the ratio is zero, it's perfect and we can stop looking
                // for a better alternative.
                //
                // NOTE:    Since 0 / x is always exactly 0.0, we do not need
                //          to handle this float in a special way.

                if (ratio == 0.0F)
                    break;
            }

            // If there is still no targetAtlas instance, there is no existing
            // atlas at all. -> We need to create one.

            if (targetAtlas == null) {
                targetAtlas = this.createAtlas(
                        this.defaultAtlasWidth,
                        this.defaultAtlasHeight);
            }
        }

        // Creating the texture fragment instance and creating the mapping

        T fragment = this.createTexture0(width, height, data, pixelFormat);

        if (!targetAtlas.insert(fragment))
            throw new IllegalStateException(
                    "Cannot insert fragment into atlas!");

        this.fragmentAssignments.put(fragment, targetAtlas);
        return fragment;
    }

    /**
     * Destroys the specified <code>texture</code> and releases its allocated
     * resources.
     *
     * @param texture   The instance that shall be destroyed.
     */
    public final void destroy(T texture) {
        A affectedAtlas = this.getAtlas(texture);

        // Removing the fragment texture from the atlas and destroying the
        // instance

        affectedAtlas.free(texture);
        this.fragmentAssignments.remove(texture);
        this.destroy0(texture);

        // Testing, if texture's occupation ratio falls bellow the
        // rearrangementThreshold.

        if (affectedAtlas.getOccupation() >= this.rearrangementThreshold)
            return;

        // Attempting to move the remaining fragments of the affectedAtlas to
        // other atlas instances.

        // TODO: Implementation

        // Testing, if the atlas is empty.
        //
        // NOTE:    Direct comparison of two floats (without boundary) should
        //          be safe here, as 1 - x / x is always exactly 0.0.

        if (affectedAtlas.getOccupation() < 0.0)
            return;

        this.destroy(affectedAtlas);
    }

    /**
     * DANGER:  Not supported yet.
     *
     * Attempts to move the specified texture <code>fragment</code> to on of
     * the specified <code>atlantes</code>.
     *
     * @param fragment  The {@link Texture} instance that shall be moved to one
     *                  of the specified <code>atlantes</code>.
     *
     * @param atlantes  The atlantes that shall contain the specified
     *                  <code>fragment</code> in the future (only one).
     *
     * @return  The new {@link TextureAtlas} instance that contains the
     *          <code>fragment</code> from now on.
     *          This may be none of the specified <code>atlantes</code>, if
     *          moving the <code>fragment</code> failed.
     */
    @SafeVarargs
    public final A optimize(T fragment, A... atlantes) {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    /**
     * Creates a new {@link TextureAtlas} instance.
     *
     * @param width     The width of the new {@link TextureAtlas}.
     * @param height    The height of the new {@link TextureAtlas}.
     * @return  The new {@link TextureAtlas} instance.
     *
     * @throws IllegalStateException    If the specified sizes exceed the
     *                                  maximum texture sizes of the current
     *                                  context.
     */
    protected abstract A createAtlas(int width, int height);

    /**
     * Destroys the specified <code>atlas</code> and releases its allocated
     * resources.
     *
     * @param atlas The {@link TextureAtlas} instance that shall be destroyed.
     *
     * @throws IllegalArgumentException If the specified <code>atlas</code> is
     *                                  unknown.
     *
     * @throws IllegalStateException    If {@link Texture} fragments had been
     *                                  assigned to the specified
     *                                  <code>atlas</code> and have not been
     *                                  removed yet.
     */
    protected abstract void destroy(A atlas);

    /**
     * Creates the corresponding instance of the implementation's
     * {@link Texture} type for the specified texture data.
     *
     * NOTE:    It has been validated that the parameters are consistent and
     *          the size (and, if possible, contents) of the passed
     *          <code>data</code> array match the other meta information.
     *
     * @param width         The width of the fragment.
     * @param height        The height of the fragment.
     * @param data          The pixel data of the fragment.
     * @param pixelFormat   The format of the specified <code>data</code>.
     *
     * @return The corresponding {@link Texture} instance for the passed data.
     *
     * @throws IllegalArgumentException If the passed parameters are invalid or
     *                                  inconsistent.
     *
     * @throws IllegalStateException    If creating the corresponding texture
     *                                  instance is not possible.
     */
    protected abstract T createTexture0(
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat);

    /**
     * Destroys the specified <code>texture</code> and releases its allocated
     * resources.
     *
     * @param texture   The texture instance that shall be destroyed.
     *
     * @throws IllegalArgumentException If the specified <code>fragment</code>
     *                                  is unknown.
     */
    protected abstract void destroy0(T texture);
}
