package org.fir3.teye;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Resources {
    public static final String SHADER_GL_MOSAIC_FRAGMENT_GLSL =
            "/org/fir3/teye/res/shader/gl/mosaic.fragment.glsl";

    public static final String SHADER_GL_MOSAIC_VERTEX_GLSL =
            "/org/fir3/teye/res/shader/gl/mosaic.vertex.glsl";

    /**
     * Reads the whole content of the specified <code>resourcePath</code> from
     * the classpath and takes it as {@link String} of the specified
     * <code>encoding</code>.
     *
     * @param resourcePath  The root-relative path of the requested resource
     *                      inside the current classpath (required to begin
     *                      with a slash).
     *
     * @param encoding      The encoding of the file's content at the specified
     *                      <code>resourcePath</code>.
     *
     * @return  The content of the file at <code>resourcePath</code> as
     *          {@link String}.
     *
     * @throws NullPointerException     If either <code>resourcePath</code> or
     *                                  <code>encoding</code> is
     *                                  <code>null</code>.

     * @throws IllegalArgumentException If <code>resourcePath</code> does not
     *                                  begin with a slash.
     *
     * @throws IllegalStateException    If the file at the specified
     *                                  <code>resourcePath</code> cannot be
     *                                  processed.
     */
    public static String readComplete(String resourcePath, Charset encoding) {
        if (resourcePath == null)
            throw new NullPointerException("resourcePath is null!");

        if (encoding == null)
            throw new NullPointerException("encoding is null!");

        // We allow only root-relative resource paths because otherwise the
        // caller could assume that the specified path is relative to its class
        // (and not to this utility class).

        if (!resourcePath.startsWith("/"))
            throw new IllegalArgumentException(
                    "Only root-relative resource paths allowed!");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             InputStream in = org.fir3.teye.util.Resources.class.getResourceAsStream(
                     resourcePath)) {
            byte[] buf = new byte[1024];
            int length;

            while ((length = in.read(buf)) > -1) {
                out.write(buf, 0, length);
            }

            return new String(buf, encoding);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read resource!");
        }
    }
}
