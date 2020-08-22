package org.fir3.teye.ui.renderer;

import java.nio.ByteBuffer;

final class DummyRenderer extends AbstractRenderer<DummyElement> {
    DummyRenderer(float oversizeFactor) {
        super(oversizeFactor);
    }

    DummyRenderer() {
        super();
    }

    @Override
    protected Element newElement0() {
        return new DummyElement();
    }

    @Override
    protected void release0(Element element) { }

    @Override
    protected boolean notifyModified0(
            DummyElement modified,
            ElementModification modification) {
        return true;
    }

    @Override
    public void initialize() { }

    @Override
    public void destroy() { }

    @Override
    public void render() { }

    @Override
    public Texture newTexture(int width, int height, ByteBuffer data, ColorModel pixelFormat) {
        return new DummyTexture(width, height, pixelFormat, data);
    }

    @Override
    public void release(Texture texture) { }
}
