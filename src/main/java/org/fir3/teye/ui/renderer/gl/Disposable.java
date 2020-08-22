package org.fir3.teye.ui.renderer.gl;

/**
 * A resource that requires advanced cleanup that is not covered by the JVM by
 * default.
 */
public interface Disposable {
    /**
     * Disposes the resource.
     */
    void dispose();
}
