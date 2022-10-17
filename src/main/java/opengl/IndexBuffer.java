package opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class IndexBuffer {
    private final int ibo;
    private int count;

    public IndexBuffer() {
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 0, GL_DYNAMIC_COPY);
    }

    public IndexBuffer(int[] indexes) {
        count = indexes.length;
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);
    }

    public IndexBuffer(IntBuffer indexes) {
        count = indexes.remaining();
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);
    }

    public void uploadSubData(int target, int offset, IntBuffer data) {
        glBufferSubData(target, offset, data);
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
    }

    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void dispose() {
        glDeleteBuffers(ibo);
    }

    public int getCount() {
        return count;
    }
}
