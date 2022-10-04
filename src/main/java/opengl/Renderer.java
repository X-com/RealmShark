package opengl;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void draw(VertexArray va, IndexBuffer ib, Shader shader) {
        shader.bind();
        va.bind();
        ib.bind();
        glDrawElements(GL_TRIANGLES, ib.getCount(), GL_UNSIGNED_INT, 0);
    }
}
