package opengl;

import static org.lwjgl.opengl.GL30.*;

public class VertexArray {
    private int vao;

    public VertexArray() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
    }

    public VertexArray addVertexBuffer(VertexBuffer vb, VertexBufferLayout layout) {
        bind();
        vb.bind();
        int offset = 0;
        for (int i = 0; i < layout.size(); i++) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, layout.getCount(i), layout.getType(i), layout.getNormalized(i), layout.getStride(), offset);
            offset += layout.getOffset(i);
        }
        return this;
    }

    public void bind() {
        glBindVertexArray(vao);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
    }
}
