package org.fir3.teye.ui.renderer.gl;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fir3.teye.ui.renderer.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class Mosaic implements Disposable {
    @Data
    @RequiredArgsConstructor(access = AccessLevel.NONE)
    private static class DrawingGroup {
        private final GLTextureAtlas[] textureAssignment;
        private int minSlot;
        private int maxSlot;

        DrawingGroup(boolean singleTexture) {
            int textureSlots = MosaicShader.TEXTURES_ARRAY_SIZE;

            if (singleTexture)
                textureSlots = 1;

            this.textureAssignment = new GLTextureAtlas[textureSlots];

            this.minSlot = 0;
            this.maxSlot = 0;
        }

        int getAtlasIndex(GLTextureAtlas atlas) {
            if (atlas == null)
                throw new NullPointerException("atlas is null!");

            for (int i = 0; i < this.textureAssignment.length; i++) {
                GLTextureAtlas next = this.textureAssignment[i];

                if (next == null)
                    break;

                if (next != atlas)
                    continue;

                return i;
            }

            return -1;
        }

        boolean addAtlas(GLTextureAtlas atlas) {
            if (atlas == null)
                throw new NullPointerException("atlas is null!");

            for (int i = 0; i < this.textureAssignment.length; i++) {
                if (this.textureAssignment[i] != null)
                    continue;

                this.textureAssignment[i] = atlas;
                return true;
            }

            return false;
        }
    }

    private static int ELEMENT_SIZE = 52;
    static int MAX_ELEMENTS = 100;

    private static void writeElementVertex(
            ByteBuffer dst,
            DrawingGroup group,
            GLTextureAtlas atlas,
            GLElement element,
            int vertex) {
        int x, y;
        int u, v;

        switch (vertex) {
            case 0:
                x = element.getX();
                y = element.getY();
                u = element.getTextureX();
                v = element.getTextureY();
                break;

            case 1:
                x = element.getX();
                y = element.getY() + element.getHeight();
                u = element.getTextureX();
                v = element.getTextureY() + element.getTextureHeight();
                break;

            case 2:
                x = element.getX() + element.getWidth();
                y = element.getY();
                u = element.getTextureX() + element.getTextureWidth();
                v = element.getTextureY();
                break;

            case 3:
                x = element.getX() + element.getWidth();
                y = element.getY() + element.getHeight();
                u = element.getTextureX() + element.getTextureWidth();
                v = element.getTextureY() + element.getTextureHeight();
                break;

            default:
                throw new IllegalArgumentException("Invalid vertex!");
        }

        // TODO:    The u and v coordinates also need to be adjusted to the
        //          size of the MetaTexture. (Otherwise the coordinates become
        //          invalid, if the size of the MetaTexture differs from the
        //          size of the TextureAtlas.)

        dst.putShort((short) x);
        dst.putShort((short) y);
        dst.put((byte) element.getRed());
        dst.put((byte) element.getGreen());
        dst.put((byte) element.getBlue());
        dst.put((byte) element.getAlpha());

        if (atlas == null)
            dst.put((byte) -1);
        else
            dst.put((byte) group.getAtlasIndex(atlas));

        dst.putShort((short) u);
        dst.putShort((short) v);
    }

    private final GLElement[] slots;
    private final List<DrawingGroup> drawingGroups;
    private final GLTextureManager textureManager;
    private int vaoId;
    private int vboId;

    @Getter(AccessLevel.PACKAGE)
    private int minZIndex;

    @Getter(AccessLevel.PACKAGE)
    private int maxZIndex;

    Mosaic(GLTextureManager textureManager) {
        if (textureManager == null)
            throw new NullPointerException("textureManager is null!");

        this.textureManager = textureManager;
        this.slots = new GLElement[Mosaic.MAX_ELEMENTS];
        this.drawingGroups = new ArrayList<>();

        // TODO:    Adding some padding at the beginning and the end of slots
        //          may improve the performance, if somebody tries to put a
        //          GLElement into the middle (slot index) of a Mosaic.
    }

    @Override
    public void dispose() {
        GL15.glDeleteBuffers(this.vboId);
        GL30.glDeleteVertexArrays(this.vaoId);
    }

    void initialize(int elementVboId) {
        this.vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.vaoId);

        this.vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);

        // Pre-filling the buffer with zeros

        ByteBuffer buffer = BufferUtils.createByteBuffer(
                Mosaic.ELEMENT_SIZE * Mosaic.MAX_ELEMENTS);

        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);

        // Setting up the vertex attribute pointers (interleaved data)

        GL20.glVertexAttribPointer(
                MosaicShader.ATTRIB_LOCATION_V_POSITION,
                2, GL11.GL_SHORT,
                false,
                13, 0);

        GL20.glVertexAttribPointer(
                MosaicShader.ATTRIB_LOCATION_V_COLOR,
                4, GL11.GL_UNSIGNED_BYTE,
                false,
                13, 4);

        GL20.glVertexAttribPointer(
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_INDEX,
                1, GL11.GL_BYTE,
                false,
                13, 8);

        GL20.glVertexAttribPointer(
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_POSITION,
                2, GL11.GL_SHORT,
                false,
                13, 9);

        GL20.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementVboId);
        GL30.glBindVertexArray(0);
    }

    GLElement insert(GLElement newElement) {
        if (newElement == null)
            throw new NullPointerException("newElement is null!");

        // Determining the right slot index

        int slotIndex;
        int newElementZIndex = newElement.getZIndex();

        for (slotIndex = this.slots.length; slotIndex > 0; slotIndex--) {
            GLElement nextSlot = this.slots[slotIndex - 1];

            if (nextSlot == null || nextSlot.getZIndex() >= newElementZIndex)
                continue;

            break;
        }

        // If the determined slot index is equal to the number of slots, there
        // is no fitting slot.

        if (slotIndex == this.slots.length)
            return newElement;

        // Otherwise, we insert the element and move the replaced elements.
        // Also, we determine the greatest slot index that has been modified
        // by inserting the newElement. (Later, when we upload the modified VBO
        // data, we only need to update those vertices that actually have been
        // modified.)

        GLElement replacedElement = this.slots[slotIndex];
        this.slots[slotIndex] = newElement;

        for (int i = slotIndex + 1; i < this.slots.length; i++) {
            if (replacedElement == null)
                break;

            GLElement oldElement = this.slots[i];
            this.slots[i] = replacedElement;

            replacedElement = oldElement;
        }

        // Determining the new lowest and highest z-index; regenerating the
        // drawing groups; updating the sub buffer

        this.determineZBoundaries();
        this.generateDrawingGroups();
        this.updateBuffer();

        return replacedElement;
    }

    void render() {
        GL30.glBindVertexArray(this.vaoId);

        GL20.glEnableVertexAttribArray(
                MosaicShader.ATTRIB_LOCATION_V_POSITION);

        GL20.glEnableVertexAttribArray(MosaicShader.ATTRIB_LOCATION_V_COLOR);
        GL20.glEnableVertexAttribArray(
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_INDEX);

        GL20.glEnableVertexAttribArray(
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_POSITION);

        for (DrawingGroup group : this.drawingGroups) {
            // Binding the textures

            GLTextureAtlas[] assignments = group.getTextureAssignment();

            for (int i = 0; i < assignments.length; i++) {
                GLTextureAtlas assignment = assignments[i];

                if (assignment == null)
                    continue;

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                this.textureManager.bind(assignment);
            }

            // Drawing the group

            int minSlot = group.getMinSlot();
            int maxSlot = group.getMaxSlot();

            GL11.glDrawElements(
                    GL11.GL_TRIANGLES,
                    (maxSlot - minSlot + 1) * 6,
                    GL11.GL_UNSIGNED_SHORT,
                    minSlot * 6 * 2);
        }
    }

    boolean contains(GLElement element) {
        for (GLElement potentialElement : this.slots) {
            if (potentialElement != element)
                continue;

            return true;
        }

        return false;
    }

    void remove(GLElement element) {
        int index;

        for (index = 0; index < this.slots.length; index++) {
            if (this.slots[index] != element)
                continue;

            break;
        }

        if (index == this.slots.length)
            throw new IllegalArgumentException("element unknown!");

        // Move up

        for (int i = index; i < this.slots.length; i++) {
            if (i + 1 < this.slots.length) {
                GLElement value = this.slots[i + 1];
                this.slots[i] = value;

                if (value == null)
                    break;

                this.slots[i + 1] = null;
                continue;
            }

            this.slots[i] = null;
        }

        // Determining the new lowest and highest z-index; regenerating the
        // drawing groups; updating the sub buffer

        this.determineZBoundaries();
        this.generateDrawingGroups();
        this.updateBuffer();
    }

    void update(GLElement element) {
        this.generateDrawingGroups();
        this.updateBuffer();
    }

    private void determineZBoundaries() {
        this.minZIndex = 0;
        this.maxZIndex = 0;

        for (int i = 0; i < this.slots.length; i++) {
            GLElement slot = this.slots[i];

            if (slot == null)
                continue;

            this.minZIndex = slot.getZIndex();
            break;
        }

        for (int i = 0; i < this.slots.length; i++) {
            GLElement slot = this.slots[this.slots.length - i  - 1];

            if (slot == null)
                continue;

            this.maxZIndex = slot.getZIndex();
            break;
        }
    }

    private void generateDrawingGroups() {
        this.drawingGroups.clear();

        boolean singleTexture = (this.textureManager.getMode() ==
                GLTextureManager.Mode.LowResource);

        DrawingGroup currentGroup = null;

        for (int i = 0; i < this.slots.length; i++) {
            GLElement slot = this.slots[i];

            if (slot == null) {
                if (currentGroup != null)
                    this.drawingGroups.add(currentGroup);

                currentGroup = null;
                continue;
            }

            if (currentGroup == null) {
                currentGroup = new DrawingGroup(singleTexture);
                currentGroup.setMinSlot(i);
            }

            // Test, if either the elements texture is null, if it is already
            // part of the current drawing group, or if we have an unoccupied
            // texture unit left.

            Texture elTexture = slot.getTexture();

            if (elTexture == null) {
                currentGroup.setMaxSlot(i);
                continue;
            }

            GLTextureAtlas atlas = this.textureManager.getAtlas(
                    (GLTexture) elTexture);

            if (currentGroup.getAtlasIndex(atlas) > -1 ||
                    currentGroup.addAtlas(atlas)) {
                currentGroup.setMaxSlot(i);
                continue;
            }

            // We need to create a new group

            this.drawingGroups.add(currentGroup);

            currentGroup = new DrawingGroup(singleTexture);
            currentGroup.setMinSlot(i);
            currentGroup.setMaxSlot(i);
            currentGroup.addAtlas(atlas);
        }

        if (currentGroup != null)
            this.drawingGroups.add(currentGroup);
    }

    private void updateBuffer() {
        // TODO:    Make this obsolet.

        this.updateBuffer(0, Mosaic.MAX_ELEMENTS);
    }

    private void updateBuffer(int slotOffset, int slotCount) {
        ByteBuffer buf = BufferUtils.createByteBuffer(
                Mosaic.ELEMENT_SIZE * slotCount);

        for (int i = slotOffset; i < (slotOffset + slotCount); i++) {
            GLElement slot = this.slots[i];

            if (slot == null) {
                buf.position(buf.position() + Mosaic.ELEMENT_SIZE);
                continue;
            }

            // Determining the drawing group

            DrawingGroup group = null;

            for (DrawingGroup potentialGroup : this.drawingGroups) {
                if (potentialGroup.getMinSlot() > i)
                    continue;

                if (potentialGroup.getMaxSlot() < i)
                    continue;

                group = potentialGroup;
                break;
            }

            // Resolving the atlas

            GLTextureAtlas atlas = null;
            GLTexture texture = (GLTexture) slot.getTexture();

            if (texture != null)
                atlas = this.textureManager.getAtlas(texture);

            // Writing the for vertices to the buffer

            for (int v = 0; v < 4; v++)
                Mosaic.writeElementVertex(buf, group, atlas, slot, v);
        }

        buf.flip();

        // Updating the VBO data

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);

        int dataOffset = slotOffset * Mosaic.ELEMENT_SIZE;
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, dataOffset, buf);
    }
}
