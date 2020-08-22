package org.fir3.teye.ui;

import org.fir3.teye.context.Context;
import org.fir3.teye.context.RenderApi;
import org.fir3.teye.ui.renderer.Renderer;
import org.fir3.teye.ui.renderer.gl.GLRenderer;

public final class UI implements ModificationListener<
        Renderer<Modification.NullModification>,
        Modification.NullModification> {
    private static UI INSTANCE;

    public static UI create(RenderApi renderApi, int width, int height) {
        if (UI.INSTANCE != null)
            throw new IllegalStateException("Already initialized!");

        return (UI.INSTANCE = new UI(renderApi, width, height));
    }

    public static UI getInstance() {
        if (UI.INSTANCE == null)
            throw new IllegalStateException("Not initialized!");

        return UI.INSTANCE;
    }

    private final Renderer<Modification.NullModification> renderer;

    private UI(RenderApi renderApi, int width, int height) {
        switch (renderApi) {
            case OpenGL:
                this.renderer = new GLRenderer(width, height);
                break;

            case OpenGL_ES:
                // TODO
            default:
                throw new UnsupportedOperationException("Unknown renderApi!");
        }
    }

    public void initialize() {
        this.requireContextThread();

        this.renderer.setModificationListener(this);
        this.renderer.initialize();
    }

    public void render() {
        this.renderer.render();
    }

    public void destroy() {
        this.requireContextThread();
        this.renderer.destroy();
    }

    private void requireContextThread() {
        if (Context.inContextThread())
            return;

        throw new IllegalStateException("Not inside context!");
    }

    @Override
    public void notifyModified(
            Renderer<Modification.NullModification> modified,
            Modification.NullModification modification) {
        Context.wakeup();
    }

    @Override
    public Modification.NullModification newModification() {
        return Modification.NullModification.INSTANCE;
    }

    @Override
    public void releaseModification(
            Modification.NullModification modification,
            boolean reuse) {
        if (reuse)
            return;

        throw new IllegalArgumentException("reuse always required!");
    }
}
