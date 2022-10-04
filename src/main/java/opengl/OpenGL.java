package opengl;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGL {
    private final int WIDTH = 1024;
    private final int HEIGHT = 768;

    private long window;
    private Shader shader;
    private VertexArray va;
    private IndexBuffer ib;

    public void run() throws InterruptedException {
        makeWindow();
        vertex();
        setupShaders();
        setupTextures();
        render();
    }

    private void makeWindow() {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(500, 500, "OpenGL window", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to open GLFW window. If you have an Intel GPU, they are not 3.3 compatible. Try the 2.1 version of the tutorials.");
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        System.out.println("Using GL Version: " + glGetString(GL_VERSION));

        // Open a window and create its OpenGL context
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidMode.width() - WIDTH) / 2, (vidMode.height() - HEIGHT) / 2);
//        glfwSetKeyCallback(window, new KeyInput()); // will use other key systems

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glfwShowWindow(window);
    }

    void vertex() {
        float[] vertices = new float[]{
                -1, -1, 0, 1,
                1, -1, 1, 1,
                1, 1, 1, 0,
                -1, 1, 0, 0,
        };

        int[] indexes = new int[]{
                0, 1, 2,
                2, 3, 0
        };

        va = new VertexArray();
        VertexBuffer vb = new VertexBuffer(vertices);
        ib = new IndexBuffer(indexes);
        VertexBufferLayout layout = new VertexBufferLayout();
        layout.addFloat(2); //vertex coords
        layout.addFloat(2); //texture coords
        va.addVertexBuffer(vb, layout);
    }

    private void setupShaders() {
        shader = new Shader("shader/basic.vert", "shader/basic.frag");
    }

    private void setupTextures() {
        Texture texture = new Texture("assets/map/map1.png");
        texture.bind(0);
        shader.setUniform1i("u_Texture", 0);
    }

    private void render() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA);
        glEnable(GL_BLEND);

        Renderer renderer = new Renderer();

        Matrix4f proj = new Matrix4f();
        proj.ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);

        Matrix4f view = new Matrix4f();

        Matrix4f model = new Matrix4f();

        Matrix4f mvp = proj.mul(view).mul(model);

        shader.setUniformMat4f("u_MVP", mvp);

        do {
            fps();

            renderer.clear();
            renderer.draw(va, ib, shader);

            glfwSwapBuffers(window); // Update Window
            glfwPollEvents(); // Key Mouse Input
        } while (glfwGetKey(window, GLFW_KEY_ESCAPE) != GLFW_PRESS && !glfwWindowShouldClose(window));

        glfwTerminate();
    }

    long time;
    public void fps() {
        long now = System.nanoTime();
        System.out.println((now - time) / 1000000f);
        time = now;
    }

    public static void main(String[] args) throws InterruptedException {
        new OpenGL().run();
    }
}
