package potato.view.opengl;

import org.joml.Matrix4f;
import potato.model.Bootloader;
import potato.model.Config;

import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

public class GLMapRender {

    private Shader shader;
    private VertexArray vaMap;
    private Texture[] textureMaps;
    private int mapIndex = 0;
    private Matrix4f mvp;
    private boolean refreshMVP;
    private boolean refreshAlpha;
    private int alpha;

    public GLMapRender() {
        shader = new Shader("shader/map.vert", "shader/map.frag");
        shader.bind();
        shader.setUniform1i("uTexImage", 0);
        shader.setUniform1f("alpha", Config.instance.mapTransparency / 255f);

        float[] mapVertices = new float[]{0, 0, 0, 1, 2048, 0, 1, 1, 2048, 2048, 1, 0, 0, 2048, 0, 0};
        int[] mapIndexes = new int[]{0, 1, 2, 2, 3, 0};

        vaMap = new VertexArray();
        VertexBuffer vb = new VertexBuffer(mapVertices);
        IndexBuffer ib = new IndexBuffer(mapIndexes);
        vaMap.setIndexBuffer(ib);
        VertexBufferLayout layout = new VertexBufferLayout();
        layout.addFloat(2); //vertex coords
        layout.addFloat(2); //texture coords
        vaMap.addVertexBuffer(vb, layout);

        BufferedImage[] maps = Bootloader.loadMaps();
        textureMaps = new Texture[maps.length];
        for (int i = 0; i < maps.length; i++) {
            int w = maps[i].getWidth();
            int h = maps[i].getHeight();
            BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            b.getGraphics().drawImage(maps[i], 0, 0, null);
            textureMaps[i] = new Texture(b, false);
        }
    }

    public void setMVP(Matrix4f mvp) {
        this.mvp = mvp;
        refreshMVP = true;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        refreshAlpha = true;
    }

    public void setMap(int mapIndex) {
        this.mapIndex = mapIndex;
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void draw() {
        shader.bind();

        if (refreshMVP) {
            shader.setUniformMat4f("uMVP", mvp);
            refreshMVP = false;
        }

        if (refreshAlpha) {
            shader.setUniform1f("alpha", alpha / 255f);
            refreshMVP = false;
        }

        textureMaps[mapIndex].bind(0);
        vaMap.bind();
        glDrawElements(GL_TRIANGLES, vaMap.getIndexBuffer().getCount(), GL_UNSIGNED_INT, 0);
    }

    public void dispose() {
        shader.dispose();
        vaMap.dispose();
        for (Texture textureMap : textureMaps) {
            textureMap.dispose();
        }
    }
}
