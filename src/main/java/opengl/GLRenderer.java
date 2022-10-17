package opengl;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

public class GLRenderer {

    private VertexArray vao;
    private IndexBuffer ibo;
    private VertexBuffer vbo;
    private Shader shader;

    private FloatBuffer vertices;
    private IntBuffer indexes;
    private int numIndexes;
    private boolean drawing;

    public GLRenderer() {
    }

    public GLRenderer(String shaderName) {
        shader = new Shader("shader/" + shaderName + ".vert", "shader/" + shaderName + ".frag");
        shader.bind();
        shader.setUniform1i("uTexture", 0);

        vertices = MemoryUtil.memAllocFloat(4096);
        indexes = MemoryUtil.memAllocInt(1024);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param texture   Used for getting width and height of the texture
     * @param x         X position of the texture
     * @param y         Y position of the texture
     * @param regX      X position of the texture region
     * @param regY      Y position of the texture region
     * @param regWidth  Width of the texture region
     * @param regHeight Height of the texture region
     * @param c         The color to use
     */
    public void drawTextureRegion(Texture texture, float x, float y, float regX, float regY, float regWidth, float regHeight, GLColor c) {
        /* Vertex positions */
        float x1 = x;
        float y1 = y;
        float x2 = x + regWidth;
        float y2 = y + regHeight;

        /* Texture coordinates */
        float s1 = regX / texture.getWidth();
        float t1 = regY / texture.getHeight();
        float s2 = (regX + regWidth) / texture.getWidth();
        float t2 = (regY + regHeight) / texture.getHeight();

        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, c);
//        drawTextureRegion(-1, -1, 1, 1, 0, 0, 1, 1, c);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param x1 Bottom left x position
     * @param y1 Bottom left y position
     * @param x2 Top right x position
     * @param y2 Top right y position
     * @param s1 Bottom left s coordinate
     * @param t1 Bottom left t coordinate
     * @param s2 Top right s coordinate
     * @param t2 Top right t coordinate
     * @param c  The color to use
     */
    public void drawTextureRegion(float x1, float y1, float x2, float y2, float s1, float t1, float s2, float t2, GLColor c) {
        if (vertices.remaining() < 32) {
            /* We need more space in the buffer, so flush it */
            flush();
        }

        float r = c.getRed();
        float g = c.getGreen();
        float b = c.getBlue();
        float a = c.getAlpha();

        vertices.put(x1).put(y1).put(s1).put(t2).put(r).put(g).put(b).put(a);
        vertices.put(x2).put(y1).put(s2).put(t2).put(r).put(g).put(b).put(a);
        vertices.put(x2).put(y2).put(s2).put(t1).put(r).put(g).put(b).put(a);
        vertices.put(x1).put(y2).put(s1).put(t1).put(r).put(g).put(b).put(a);

        int i = numIndexes / 6;

        indexes.put(i * 4).put(1 + i * 4).put(2 + i * 4).put(2 + i * 4).put(3 + i * 4).put(i * 4);

        numIndexes += 6;
    }

    /**
     * Begin rendering.
     */
    public void begin() {
        if (drawing) {
            throw new IllegalStateException("Renderer is already drawing!");
        }
        drawing = true;
        numIndexes = 0;
    }

    /**
     * End rendering.
     */
    public void end() {
        if (!drawing) {
            throw new IllegalStateException("Renderer isn't drawing!");
        }
        drawing = false;
        flush();
    }

    /**
     * Flushes the data to the GPU to let it get rendered.
     */
    public void flush() {
        if (numIndexes > 0) {
            vertices.flip();
            indexes.flip();

            if (vao != null) {
                vao.bind();
            } else {
                specifyVertexAttributes();
            }
            shader.bind();

            /* Upload the new vertex data */
            ibo.bind();
            ibo.uploadSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexes);
            vbo.bind();
            vbo.uploadSubData(GL_ARRAY_BUFFER, 0, vertices);

            /* Draw batch */
            glDrawElements(GL_TRIANGLES, numIndexes, GL_UNSIGNED_INT, 0);

            /* Clear vertex data for next batch */
            vertices.clear();
            indexes.clear();
            numIndexes = 0;
        }
    }

    /**
     * Specifies the vertex pointers.
     */
    private void specifyVertexAttributes() {
        vao = new VertexArray();
        vbo = new VertexBuffer(vertices);
        ibo = new IndexBuffer(indexes);

        VertexBufferLayout layout = new VertexBufferLayout();
        layout.addFloat(2); //vertex coords
        layout.addFloat(2); //texture coords
        layout.addFloat(4); //color
        vao.addVertexBuffer(vbo, layout);
    }

    public void setMVP(Matrix4f mvp) {
        shader.bind();
        shader.setUniformMat4f("uMVP", mvp);
    }

    public static void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void draw(VertexArray va, IndexBuffer ib, Shader shader) {
        shader.bind();
        va.bind();
        ib.bind();
        glDrawElements(GL_TRIANGLES, ib.getCount(), GL_UNSIGNED_INT, 0);
    }

    public void dispose() {
        MemoryUtil.memFree(vertices);
        MemoryUtil.memFree(indexes);

        vao.dispose();
        vbo.dispose();
        shader.dispose();
    }
}
