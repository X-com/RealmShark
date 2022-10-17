package opengl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;


public class Shader {
    private int program;
    private final HashMap<String, Integer> uniformLocationCache = new HashMap<>();
    private static final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

    public Shader(String vertexFilePath, String fragmentFilePath) {
        program = glCreateProgram();

        String vsCode = readShader(vertexFilePath);
        String fsCode = readShader(fragmentFilePath);

        createShader(vsCode, fsCode);
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void dispose() {
        glDeleteProgram(program);
    }

    public static String readShader(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Failed to read file: " + path);
            e.printStackTrace();
            throw new RuntimeException("Shader failed to load.");
        }
        StringBuilder out = new StringBuilder();
        lines.forEach((line) -> out.append(line).append("\n"));

        return out.toString();
    }

    private void createShader(String vsCode, String fsCode) {
        int vs = compileShader(GL_VERTEX_SHADER, vsCode);
        int fs = compileShader(GL_FRAGMENT_SHADER, fsCode);

        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glValidateProgram(program);

        glDeleteShader(vs);
        glDeleteShader(fs);
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

    public void setUniform4f(String name, float f0, float f1, float f2, float f3) {
        int id = glGetUniformLocation(program, name);
        glUniform4f(id, f0, f1, f2, f3);
    }

    public void setUniform1i(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    public void setUniformMat4f(String name, Matrix4f matrix) {
        glUniformMatrix4fv(getUniformLocation(name), false, matrix.get(matrixBuffer));
    }

    public int getUniformLocation(String name) {
        Integer i = uniformLocationCache.get(name);
        if (i != null) return i;

        int location = glGetUniformLocation(program, name);
        if (location == -1) {
            System.out.println("No active uniform variable with name " + name + " found");
            return -1;
        }
        uniformLocationCache.put(name, location);

        return location;
    }
}
