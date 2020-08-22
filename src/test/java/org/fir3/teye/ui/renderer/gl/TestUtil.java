package org.fir3.teye.ui.renderer.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

final class TestUtil {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String WINDOW_TITLE = "Unit Test Context";

    static final String PATH_IMG_RAND = "/img/rand.png";
    static final int WIDTH_IMG_RAND = 300;
    static final int HEIGHT_IMG_RAND = 300;

    private static long windowId;

    static void createContext() {
        if (TestUtil.windowId != 0L)
            return;

        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException("GLFW initialization failed!");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,
                GLFW.GLFW_OPENGL_CORE_PROFILE);

        TestUtil.windowId = GLFW.glfwCreateWindow(
                TestUtil.WINDOW_WIDTH, TestUtil.WINDOW_HEIGHT,
                TestUtil.WINDOW_TITLE,
                MemoryUtil.NULL, MemoryUtil.NULL);

        if (TestUtil.windowId == MemoryUtil.NULL)
            throw new IllegalStateException("Window creation failed!");

        GLFW.glfwMakeContextCurrent(TestUtil.windowId);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
    }

    static void destroyContext() {
        if (TestUtil.windowId == 0L)
            return;

        Callbacks.glfwFreeCallbacks(TestUtil.windowId);
        GLFW.glfwDestroyWindow(TestUtil.windowId);
        TestUtil.windowId = 0L;

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    static ByteBuffer readImageRGBA8888(String path) {
        BufferedImage img;

        try (InputStream in = TestUtil.class.getResourceAsStream(path)) {
            img = ImageIO.read(in);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        // We need to allocate a buffer that is large enough to contain the
        // whole image and we need to convert the pixels from from ARGB to
        // RGBA.

        int width = img.getWidth();
        int height = img.getHeight();

        ByteBuffer buf = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);

                // ARGB -> RGBA

                pixel = (pixel << 8) | (pixel >> 24);
                buf.putInt(pixel);
            }
        }

        buf.flip();
        return buf;
    }

    private TestUtil() {}
}
