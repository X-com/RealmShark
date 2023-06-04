package experimental;

import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import io.github.chiraagchakravarthy.lwjgl_vectorized_text.VectorFont;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class TextRendererTest {
    private final int WIDTH = 1000;
    private final int HEIGHT = 700;
    private long window;

    public void run() {
        makeWindow();
        render();
    }

    private void makeWindow() {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        System.setProperty("java.awt.headless", "true");
        glfwWindowHint(GLFW_SAMPLES, 1);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "OpenGL window", NULL, NULL);
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
        //glfwSetKeyCallback(window, new KeyInput()); // will use other key systems

        glClearColor(1,1,1, 1.0f);
        glfwShowWindow(window);
    }

    private void render() {
        VectorFont font = new VectorFont();
        StringBuilder str = new StringBuilder();
        for (int i = '!'; i < 155; i++) {
            str.append((char) i);
        }
        String s = str.toString();
        Vector4f color = new Vector4f(0.75f, 0.75f, 0.75f, 1);
        float v = 0;

//        TextRenderer completelyUnnecessaryObject = new TextRenderer(new Matrix4f().ortho(0, WIDTH, 0, HEIGHT, -1, 1));

        do {
            v += 0.01f;
            Matrix4f pose = new Matrix4f().translate(100, 100, 0).scale(100).rotate(v, (float) Math.cos(v*Math.PI/3), (float) Math.sin(v*Math.PI/3), 0);
            fps();
            glClear(GL_COLOR_BUFFER_BIT);

            //TextRenderer.drawText("hello", 10, 100f, font, 1000, color);
//            completelyUnnecessaryObject.drawText("hello", font, pose, color);
            glfwSwapBuffers(window); // Update Window
            glfwPollEvents(); // Key Mouse Input
        } while (glfwGetKey(window, GLFW_KEY_ESCAPE) != GLFW_PRESS && !glfwWindowShouldClose(window));
        glfwTerminate();
    }
    long lastTime = -1;
    int frames;
    public void fps() {
        frames++;
        long now = System.nanoTime();
        if(lastTime == -1){
            lastTime = now;
            return;
        }
        if(now-lastTime>=1000000000) {
            System.out.println((float)(now - lastTime) / frames / 1000000f);
            lastTime += 1000000000;
            frames = 0;
        }
    }

    public static void main(String[] args) {
        new TextRendererTest().run();
    }
}