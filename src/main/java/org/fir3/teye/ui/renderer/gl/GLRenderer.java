package org.fir3.teye.ui.renderer.gl;

import org.fir3.teye.ui.renderer.*;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GLRenderer extends AbstractRenderer<GLElement> {
    private final GLTextureManager textureManager;
    private final MosaicShader shader;
    private final List<Mosaic> mosaics;
    private final int width, height;
    private int elementVboId;

    public GLRenderer(int width, int height) {
        if (width < 1)  throw new IllegalArgumentException("Invalid width!");
        if (height < 1) throw new IllegalArgumentException("Invalid height!");

        this.width = width;
        this.height = height;

        this.textureManager = new GLTextureManager();
        this.shader = new MosaicShader();
        this.mosaics = new ArrayList<>();
    }

    @Override
    protected Element newElement0() {
        GLElement el = new GLElement();

        // It is important that the z-index is at the end of the possible
        // z-indices as otherwise inserting the element could lead to a large
        // rearrangement of elements.

        el.setZIndex(Integer.MAX_VALUE);

        // NOTE:    It is important that we set the modificationListener after
        //          updating any property of the new element, otherwise the
        //          modification will lead to early adding of the new element
        //          to a mosaic (duplicate).

        el.setModificationListener(this);
        this.updateMosaic(el, false);

        return el;
    }

    @Override
    protected void release0(Element element) {
        GLElement glElement = (GLElement) element;

        for (Mosaic mosaic : this.mosaics) {
            if (!mosaic.contains(glElement))
                continue;

            mosaic.remove(glElement);
            break;
        }
    }

    @Override
    protected boolean notifyModified0(
            GLElement modified,
            ElementModification modification) {
        if (modification.modifiedAttribute() == ElementAttribute.Z_INDEX) {
            this.updateMosaic(modified, true);
            return true;
        }

        for (Mosaic mosaic : this.mosaics) {
            if (!mosaic.contains(modified))
                continue;

            mosaic.update(modified);
            break;
        }

        return true;
    }

    @Override
    public void initialize() {
        GL11.glViewport(0, 0, this.width, this.height);

        this.shader.initialize();

        // Setting the projection matrix once and for all

        Matrix4f projMat = new Matrix4f();
        projMat.identity();
        projMat.setOrtho2D(0.0F, this.width, this.height, 0.0F);

        this.shader.setProjectionMatrix(projMat);

        // Creating the VBO that contains the element indices

        this.elementVboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.elementVboId);

        ShortBuffer indices = BufferUtils.createShortBuffer(
                Mosaic.MAX_ELEMENTS * 6);

        for (int i = 0; i < Mosaic.MAX_ELEMENTS; i++) {
            int base = i * 4;

            // First triangle

            indices.put((short) (base));
            indices.put((short) (base + 1));
            indices.put((short) (base + 2));

            // Second triangle

            indices.put((short) (base + 2));
            indices.put((short) (base + 1));
            indices.put((short) (base + 3));
        }

        indices.flip();

        GL15.glBufferData(
                GL15.GL_ELEMENT_ARRAY_BUFFER,
                indices,
                GL15.GL_STATIC_DRAW);

        // Enabling alpha blending

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Only draw the front face (vertices specified in counter-clock-wise
        // order)

        GL11.glEnable(GL11.GL_CULL_FACE);

        // Setting the clear color to white

        GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void destroy() {
        GL15.glDeleteBuffers(this.elementVboId);

        this.shader.dispose();
        this.textureManager.dispose();
    }

    @Override
    public void render() {
        this.shader.use();

        // TODO:    Only re-render the part that has actually been updated.

        // Clearing the scene

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // Rendering the element collections

        for (Mosaic mosaic : this.mosaics)
            mosaic.render();
    }

    @Override
    public Texture newTexture(
            int width, int height,
            ByteBuffer data,
            ColorModel pixelFormat) {
        return this.textureManager.createTexture(
                width, height,
                data,
                pixelFormat);
    }

    @Override
    public void release(Texture texture) {
        this.textureManager.destroy((GLTexture) texture);
    }

    private void updateMosaic(GLElement element, boolean removeExisting) {
        if (removeExisting) {
            for (Mosaic mosaic : this.mosaics) {
                if (!mosaic.contains(element))
                    continue;

                mosaic.remove(element);
                break;
            }
        }

        int zIndex = element.getZIndex();
        Iterator<Mosaic> it = this.mosaics.iterator();

        while (it.hasNext()) {
            Mosaic mosaic = it.next();

            if (element == null)
                break;

            if (mosaic.getMaxZIndex() < zIndex && it.hasNext())
                continue;

            element = mosaic.insert(element);

            if (element != null)
                zIndex = element.getZIndex();
        }

        if (element == null)
            return;

        Mosaic newMosaic = new Mosaic(this.textureManager);
        newMosaic.initialize(this.elementVboId);
        newMosaic.insert(element);

        this.mosaics.add(newMosaic);
    }
}
