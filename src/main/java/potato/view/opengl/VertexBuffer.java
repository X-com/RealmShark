package potato.view.opengl;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class VertexBuffer {
    private int vbo;
    private boolean dynamic;
    private float[] vertices;

    public VertexBuffer() {
        this.dynamic = true;
        vbo = glGenBuffers();
        bind();
        glBufferData(GL_ARRAY_BUFFER, 0, GL_DYNAMIC_COPY);
    }

    public VertexBuffer(float[] vertices) {
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    public VertexBuffer(FloatBuffer vertices) {
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    public VertexBuffer(float[] vertices, boolean dynamic) {
        this.dynamic = dynamic;
        this.vertices = vertices;
        vbo = glGenBuffers();
        bind();
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_COPY);
    }

    public VertexBuffer(FloatBuffer vertices, boolean dynamic) {
        this.dynamic = dynamic;
        vbo = glGenBuffers();
        bind();
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_COPY);
    }

    public void updateDynamicVertices() {
        if(!dynamic) throw new RuntimeException("Editing static vertex buffer.");
        bind();
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_COPY);
    }

    public float[] getDynamicVertices() {
        if(!dynamic) throw new RuntimeException("Static vertex buffer.");
        return vertices;
    }

    /**
     * Upload sub data to this VBO with specified target, offset and data.
     *
     * @param target Target to upload
     * @param offset Offset where the data should go in bytes
     * @param data   Buffer with the data to upload
     */
    public void uploadSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
    }

    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void dispose() {
        glDeleteBuffers(vbo);
    }
}
