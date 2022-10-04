package opengl;

import static org.lwjgl.opengl.GL15.*;

public class IndexBuffer {
    private final int ibo;
    private final int count;

    public IndexBuffer(int[] indexes) {
        count = indexes.length;
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);
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
