package org.fir3.teye.context;

import org.fir3.teye.ui.UI;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.util.LinkedList;
import java.util.Queue;

public final class Context implements Runnable {
    private static final String THREAD_NAME = "Context";
    private static final String WINDOW_TITLE = "Mediashare Context";

    private static Context INSTANCE;

    public static synchronized void create(
            int width, int height,
            RenderApi renderApi) {
        if (Context.INSTANCE != null)
            throw new IllegalStateException(
                    "Context has been created already!");

        Context.INSTANCE = new Context(width, height, renderApi);

        Thread contextThread = new Thread(
                Context.INSTANCE,
                Context.THREAD_NAME);

        contextThread.start();
    }

    public static void execute(Runnable task) {
        if (Context.inContextThread()) {
            task.run();
            return;
        }

        synchronized (Context.INSTANCE.tasks) {
            Context.INSTANCE.tasks.offer(task);
        }

        Context.wakeup();
    }

    public static boolean inContextThread() {
        Context ctx = Context.INSTANCE;

        if (ctx == null)
            return false;

        return Thread.currentThread().getId() == ctx.contextThread.getId();
    }

    public static void wakeup() {
        GLFW.glfwPostEmptyEvent();
    }

    private final int width;
    private final int height;
    private final RenderApi renderApi;
    private final Queue<Runnable> tasks;

    private Thread contextThread;
    private long windowPtr;

    private Context(int width, int height, RenderApi renderApi) {
        this.width = width;
        this.height = height;
        this.renderApi = renderApi;
        this.tasks = new LinkedList<>();
    }

    @Override
    public void run() {
        this.contextThread = Thread.currentThread();

        // Initializing GLFW

        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException("Cannot initialize GLFW!");

        // Creating the GLFW window

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        switch (this.renderApi) {
            case OpenGL:
                GLFW.glfwWindowHint(
                        GLFW.GLFW_CLIENT_API,
                        GLFW.GLFW_OPENGL_API);

                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
                GLFW.glfwWindowHint(
                        GLFW.GLFW_OPENGL_FORWARD_COMPAT,
                        GLFW.GLFW_TRUE);

                GLFW.glfwWindowHint(
                        GLFW.GLFW_OPENGL_PROFILE,
                        GLFW.GLFW_OPENGL_CORE_PROFILE);
                break;

            case OpenGL_ES:
                // TODO
                break;

            default:
                throw new UnsupportedOperationException("Unknown render API!");
        }

        this.windowPtr = GLFW.glfwCreateWindow(
                this.width, this.height,
                Context.WINDOW_TITLE,
                MemoryUtil.NULL, MemoryUtil.NULL);

        if (this.windowPtr == MemoryUtil.NULL)
            throw new IllegalStateException("Failed creating window!");

        // Context initialization

        GLFW.glfwMakeContextCurrent(this.windowPtr);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        // Initializing the UI

        UI ui = UI.create(this.renderApi, this.width, this.height);
        ui.initialize();

        // Entering the main loop

        while (true) {
            if (GLFW.glfwWindowShouldClose(this.windowPtr))
                break;

            synchronized (this.tasks) {
                while (!this.tasks.isEmpty())
                    this.tasks.poll().run();
            }

            ui.render();

            GLFW.glfwSwapBuffers(this.windowPtr);
            GLFW.glfwWaitEvents();
        }

        // Destroying the UI

        ui.destroy();

        // Destroying the context

        Callbacks.glfwFreeCallbacks(this.windowPtr);
        GLFW.glfwDestroyWindow(this.windowPtr);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }
}
