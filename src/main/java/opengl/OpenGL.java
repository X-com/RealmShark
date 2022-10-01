package opengl;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGL {
    private final int WIDTH = 1024;
    private final int HEIGHT = 768;

    private long window;
    private int color;

    public void run() throws InterruptedException {
        makeWindow();
        vertex();
        setupShaders();
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
        window = glfwCreateWindow(1024, 768, "OpenGL window", NULL, NULL);
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
        float[] vertices = new float[] {
                -0.5f, -0.5f, 0, 1,
                 0.5f, -0.5f, 1, 1,
                 0.5f,  0.5f, 1, 0,
                -0.5f,  0.5f, 0, 0
        };

        int[] indexes = new int[] {
                0, 1, 2,
                2, 3, 0
        };

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int buffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0); //vertex coords
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);

        glEnableVertexAttribArray(1); //texture coords
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);

        int ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);
    }

    private void setupShaders() {
        int shader = createShader("shader/basic.vert", "shader/basic.frag");
        color = glGetUniformLocation(shader, "u_Color");
        glUniform1i(color, 0);
        glUseProgram(shader);
    }

    private int createShader(String vsPath, String fsPath) {
        int program = glCreateProgram();
        String vsCode;
        try {
            vsCode = readFile(vsPath);
        } catch (IOException e) {
            System.out.println("Failed to read vertex shader: \"" + vsPath + "\"");
            return 0;
        }

        String fsCode;
        try {
            fsCode = readFile(fsPath);
        } catch (IOException e) {
            System.out.println("Failed to read fragment shader: \"" + fsPath + "\"");
            return 0;
        }

        int vs = compileShader(GL_VERTEX_SHADER, vsCode);
        int fs = compileShader(GL_FRAGMENT_SHADER, fsCode);

        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glValidateProgram(program);

        glDeleteShader(vs);
        glDeleteShader(fs);

        return program;
    }

    public static String readFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        StringBuilder out = new StringBuilder();
        lines.forEach((line) -> out.append(line).append("\n"));
        return out.toString();
    }

    private int compileShader(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        int[] result = new int[1];
        glGetShaderiv(id, GL_COMPILE_STATUS, result);
        if (result[0] == GL_FALSE) {
            String message = glGetShaderInfoLog(id);
            System.out.println("Failed to compile " + (type == GL_VERTEX_SHADER ? "vertex" : "fragment") + " shader");
            System.out.println(message);
            glDeleteShader(id);
            return 0;
        }
        return id;
    }

    private void render() {
        float red = 0.0f;
        float step = 0.05f;
        long time = System.nanoTime();

        do {
            long now = System.nanoTime();
            System.out.println((now - time)/1000000f);
            time = now;

            // Clear the screen
            glClear(GL_COLOR_BUFFER_BIT);

            // set uniform color
            glUniform4f(color, red, 0.3f, 0.8f, 1.0f);

            // Draw the triangle !
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            // Swap buffers
            glfwSwapBuffers(window);
            glfwPollEvents();

            // increment red
            if (red < 0.0f || red > 1.0f) step *= -1.0;
            red += step;

        } while (glfwGetKey(window, GLFW_KEY_ESCAPE) != GLFW_PRESS && !glfwWindowShouldClose(window));

        glfwTerminate();
    }

    public static void main(String[] args) throws InterruptedException {
        new OpenGL().run();
    }
}
