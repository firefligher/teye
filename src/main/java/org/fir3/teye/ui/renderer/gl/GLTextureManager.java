package org.fir3.teye.ui.renderer.gl;

import lombok.AccessLevel;
import lombok.Getter;
import org.fir3.teye.ui.renderer.BaseTextureManager;
import org.fir3.teye.ui.renderer.ColorModel;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

final class GLTextureManager
        extends BaseTextureManager<GLTextureAtlas, GLTexture>
        implements Disposable {
    enum Mode {
        /**
         * If the {@link GLTextureManager} runs in normal mode, each
         * {@link GLTextureAtlas} has its own {@link MetaTexture} instance
         * (one-to-one-mapping).
         */
        Normal,

        /**
         * If the {@link GLTextureManager} runs in the low resource mode, this
         * means that no further textures can be allocated and multiple
         * {@link GLTextureAtlas} instances share the same {@link MetaTexture}
         * instance (many-to-one-mapping).
         */
        LowResource
    }

    private final Map<GLTextureAtlas, MetaTexture> assignments;

    @Getter(AccessLevel.PACKAGE)
    private Mode mode;

    GLTextureManager() {
        this.assignments = new IdentityHashMap<>();
        this.mode = Mode.Normal;
    }

    @Override
    protected GLTextureAtlas createAtlas(int width, int height) {
        // TODO:    Decide whether we make the pixelFormat of atlantes constant
        //          or configurable.

        ColorModel pixelFormat = ColorModel.RGBA_8888;
        GLTextureAtlas atlas = new GLTextureAtlas(
                width, height,
                this,
                pixelFormat);

        switch (this.mode) {
            case Normal:
                // Creating a corresponding MetaTexture

                MetaTexture peer = new MetaTexture(width, height, pixelFormat);

                if (!peer.initialize()) {
                    // The initialization of the OpenGL peer failed. This may
                    // indicate resource exhaustion, thus we change the
                    // operation mode of this manager.
                    //
                    // TODO:    Handle the case that the specified width and
                    //          height are greater then maximums of OpenGL.

                    this.mode = Mode.LowResource;
                    return this.createAtlas(width, height);
                }

                this.assignments.put(atlas, peer);
                break;

            case LowResource:
                // If we ever want to be able to use the atlas, we need a
                // fitting MetaTexture.

                boolean fittingFound = false;

                for (MetaTexture potentialTexture :
                        this.assignments.values()) {
                    int textureWidth = potentialTexture.getWidth();
                    int textureHeight = potentialTexture.getHeight();
                    ColorModel texturePixelFormat =
                            potentialTexture.getPixelFormat();

                    if (width > textureWidth ||
                            height > textureHeight ||
                            pixelFormat != texturePixelFormat)
                        continue;

                    fittingFound = true;
                    break;
                }

                if (!fittingFound)
                    throw new IllegalStateException("Resource exhaustion!");
                break;

            default:
                throw new UnsupportedOperationException("Unsupported mode!");
        }

        return atlas;
    }

    @Override
    protected void destroy(GLTextureAtlas atlas) {
        this.assignments.remove(atlas);
    }

    @Override
    protected GLTexture createTexture0(
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat) {
        // We need to duplicate the pixel buffer (data) as the caller may
        // re-use it in the future.

        byte[] dataCopy = new byte[data.remaining()];
        data.get(dataCopy);

        ByteBuffer dataCopyBuf = BufferUtils.createByteBuffer(dataCopy.length);
        dataCopyBuf.put(dataCopy);
        dataCopyBuf.flip();

        return new GLTexture(width, height, pixelFormat, dataCopyBuf);
    }

    @Override
    protected void destroy0(GLTexture texture) {
        // Since there are no non-GC resources that have been linked directly
        // to GLTexture instances, we do not need to clean up anything as the
        // GC will do this for us after the last reference to the texture
        // instance is removed.
    }

    @Override
    public void dispose() {
        for (MetaTexture texture : this.assignments.values())
            texture.dispose();

        this.assignments.clear();
    }

    /**
     * Binds the specified <code>atlas</code> to the enabled OpenGL texture
     * unit.
     *
     * @param atlas The atlas that is being bound to the enabled OpenGL texture
     *              unit
     *
     * @return  The {@link MetaTexture} instance that wraps the corresponding
     *          OpenGL texture that has been used for uploading the texture
     *          data.
     */
    MetaTexture bind(GLTextureAtlas atlas) {
        if (atlas == null) throw new NullPointerException("atlas is null!");

        MetaTexture metaTexture = this.assignments.get(atlas);

        switch (this.mode) {
            case Normal:
                if (metaTexture == null)
                    throw new IllegalArgumentException("Unknown atlas!");
                break;

            case LowResource:
                if (metaTexture == null) {
                    // Looking for a fitting OpenGL texture (at least the same
                    // width and height as the atlas, and the same pixel
                    // format)

                    int atlasWidth = atlas.getWidth();
                    int atlasHeight = atlas.getHeight();
                    ColorModel atlasPF = atlas.getPixelFormat();
                    GLTextureAtlas prevAtlas = null;

                    for (Map.Entry<GLTextureAtlas, MetaTexture> assignment :
                            this.assignments.entrySet()) {
                        MetaTexture potentialTexture = assignment.getValue();
                        int width = potentialTexture.getWidth();
                        int height = potentialTexture.getHeight();
                        ColorModel pixelFormat =
                                potentialTexture.getPixelFormat();

                        if (width < atlasWidth ||
                                height < atlasHeight ||
                                pixelFormat != atlasPF)
                            continue;

                        metaTexture = potentialTexture;
                        prevAtlas = assignment.getKey();
                        break;
                    }

                    if (metaTexture == null)
                        throw new IllegalStateException(
                                "No fitting OpenGL texture for atlas!");

                    // Saving the previous atlas into a local buffer and
                    // removing its assignment

                    prevAtlas.save(metaTexture);
                    this.assignments.remove(prevAtlas);

                    // Since the assigned metaTexture has been used by another
                    // GLTextureAtlas, we need to restore (upload) the current
                    // GLTextureAtlas.

                    this.assignments.put(atlas, metaTexture);
                    atlas.restore(metaTexture);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown mode!");
        }

        metaTexture.bind();
        return metaTexture;
    }
}
