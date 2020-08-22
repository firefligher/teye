package org.fir3.teye.ui.renderer;

import lombok.Getter;
import org.fir3.teye.ui.AbstractModifiable;

@Getter
public abstract class AbstractElement<E extends AbstractElement<E>>
        extends AbstractModifiable<E, ElementModification>
        implements Element {
    private static void checkInRange(
            int minimum, int maximum,
            int actualValue) {
        if (actualValue < minimum || actualValue > maximum)
            throw new IllegalArgumentException("Out of range!");
    }

    private static void checkGreaterEqual(int minimum, int actualValue) {
        if (actualValue < minimum)
            throw new IllegalArgumentException("Out of range!");
    }

    private int x, y, width, height,
            red, green, blue, alpha,
            textureX, textureY, textureWidth, textureHeight,
            zIndex;

    private Texture texture;

    @Override
    public void setX(int x) {
        AbstractElement.checkGreaterEqual(0, x);
        this.notifyIfModified(this.x, this.x = x, ElementAttribute.X);
    }

    @Override
    public void setY(int y) {
        AbstractElement.checkGreaterEqual(0, y);
        this.notifyIfModified(this.y, this.y = y, ElementAttribute.Y);
    }

    @Override
    public void setWidth(int width) {
        AbstractElement.checkGreaterEqual(0, width);

        this.notifyIfModified(
                this.width, this.width = width,
                ElementAttribute.WIDTH);
    }

    @Override
    public void setHeight(int height) {
        AbstractElement.checkGreaterEqual(0, height);

        this.notifyIfModified(
                this.height, this.height = height,
                ElementAttribute.HEIGHT);
    }

    @Override
    public void setRed(int red) {
        AbstractElement.checkInRange(0, 255, red);

        this.notifyIfModified(
                this.red, this.red = red,
                ElementAttribute.RED_COMPONENT);
    }

    @Override
    public void setGreen(int green) {
        AbstractElement.checkInRange(0, 255, green);

        this.notifyIfModified(
                this.green, this.green = green,
                ElementAttribute.GREEN_COMPONENT);
    }

    @Override
    public void setBlue(int blue) {
        AbstractElement.checkInRange(0, 255, blue);

        this.notifyIfModified(
                this.blue, this.blue = blue,
                ElementAttribute.BLUE_COMPONENT);
    }

    @Override
    public void setAlpha(int alpha) {
        AbstractElement.checkInRange(0, 255, alpha);

        this.notifyIfModified(
                this.alpha, this.alpha = alpha,
                ElementAttribute.ALPHA_COMPONENT);
    }

    @Override
    public void setTexture(Texture texture) {
        this.notifyIfModified(
                this.texture, this.texture = texture,
                ElementAttribute.TEXTURE);
    }

    @Override
    public void setTextureX(int x) {
        AbstractElement.checkGreaterEqual(0, x);

        this.notifyIfModified(
                this.textureX, this.textureX = x,
                ElementAttribute.TEXTURE_X);
    }

    @Override
    public void setTextureY(int y) {
        AbstractElement.checkGreaterEqual(0, y);

        this.notifyIfModified(
                this.textureY, this.textureY = y,
                ElementAttribute.TEXTURE_Y);
    }

    @Override
    public void setTextureWidth(int width) {
        AbstractElement.checkGreaterEqual(0, width);

        this.notifyIfModified(
                this.textureWidth, this.textureWidth = width,
                ElementAttribute.TEXTURE_WIDTH);
    }

    @Override
    public void setTextureHeight(int height) {
        AbstractElement.checkGreaterEqual(0, height);

        this.notifyIfModified(
                this.textureHeight, this.textureHeight = height,
                ElementAttribute.TEXTURE_HEIGHT);
    }

    @Override
    public void setZIndex(int zIndex) {
        this.notifyIfModified(
                this.zIndex, this.zIndex = zIndex,
                ElementAttribute.Z_INDEX);
    }

    private  <V> void notifyIfModified(
            V previousValue, V newValue,
            ElementAttribute modifiedAttribute) {
        ElementModification mod = this.obtainModification();

        if (mod == null)
            return;

        mod.previousValue(previousValue)
                .newValue(newValue)
                .modifiedAttribute(modifiedAttribute);

        this.notifyIfModified(previousValue, newValue, mod);
    }
}
