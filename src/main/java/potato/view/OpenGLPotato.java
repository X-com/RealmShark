package potato.view;

import opengl.*;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import potato.Potato;
import potato.model.Bootloader;
import potato.model.DataModel;
import potato.model.HeroLocations;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLPotato extends Thread {
    public boolean waitfor = true;
    private final DataModel model;
    private boolean running;

    private int width = 300;
    private int height = 300;

    private long window;
    private Shader shaderMap;
    private Shader shaderHero;
    private VertexArray vaMap;
    private VertexArray[] vaHero;
    private Texture[] textureMaps;
    private Texture textureHeroes;
    private Matrix4f mvp;
    private Matrix4f proj;
    private GLRenderer renderer;
    private boolean viewChanged;
    private int mapIndex = 0;
    public static final float[] scale = {0.855f, 1.025f, 1.275f, 5 / 3f, 2.5f, 5f, 41.2f};
    public static final float[] playerOffset = {0, 1f / 3, 0.85f, 5 / 3f, 10 / 3f, 8.5f, 84.25f};
    public static int zoom = 0;
    private float ratio;
    ArrayList<HeroLocations>[] heroLocs;

    private static boolean userShowMap = true;
    private static boolean userShowHeroes = true;
    private static boolean userShowInfo = true;
    private static boolean showMap = false;
    private static boolean showHeroes = false;
    private boolean showHeroCount = false;
    public static boolean refresh = false;

    // temp //
    long time;

    public void fps() {
        long now = System.nanoTime();
        System.out.println((now - time) / 1000000f);
        time = now;
    }
    // temp end //

    public static void main(String[] args) throws InterruptedException {
        new OpenGLPotato(null).run();
    }

    public OpenGLPotato(DataModel dataModel) {
        System.setProperty("java.awt.headless", "true");
        this.model = dataModel;
    }

    public void run() {
        makeWindow();
        vertexMap();
        vertexHeroes();
        setupShaders();
        setupTextures();
        preRender();
        font();
        waitfor = false;
        running = true;
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
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "OpenGL window", NULL, NULL);
        ratio = (float) width / height;
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to open GLFW window. If you have an Intel GPU, they are not 3.3 compatible. Try the 2.1 version of the tutorials.");
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        System.out.println("Using GL Version: " + glGetString(GL_VERSION));

        try {
            BufferedImage bufferedImage = ImageIO.read(Potato.imagePath);
            ByteBuffer buff = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);
            ImageParser iconPars = new ImageParser(bufferedImage.getWidth(), bufferedImage.getHeight(), buff);
            GLFWImage image = GLFWImage.malloc();
            image.set(iconPars.getWidth(), iconPars.getHeigh(), iconPars.getImage());
            GLFWImage.Buffer images = GLFWImage.malloc(1);
            images.put(0, image);
            glfwSetWindowIcon(window, images);

            images.free();
            image.free();
        } catch (IOException e) {
            e.printStackTrace();
        }

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    /**
     * Convert the {@link BufferedImage} to the {@link GLFWImage}.
     */
    private GLFWImage imageToGLFWImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            final BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            final Graphics2D graphics = convertedImage.createGraphics();
            final int targetWidth = image.getWidth();
            final int targetHeight = image.getHeight();
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
            image = convertedImage;
        }
        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int colorSpace = image.getRGB(j, i);
                buffer.put((byte) ((colorSpace << 8) >> 24));
                buffer.put((byte) ((colorSpace << 16) >> 24));
                buffer.put((byte) ((colorSpace << 24) >> 24));
                buffer.put((byte) (colorSpace >> 24));
            }
        }
        buffer.flip();
        final GLFWImage result = GLFWImage.create();
        result.set(image.getWidth(), image.getHeight(), buffer);
        return result;
    }

    void vertexMap() {
        float[] mapVertices = new float[]{
                -1024, -1024, 0, 1,
                1024, -1024, 1, 1,
                1024, 1024, 1, 0,
                -1024, 1024, 0, 0,};

        int[] mapIndexes = new int[]{0, 1, 2, 2, 3, 0,};

        vaMap = new VertexArray();
        VertexBuffer vb = new VertexBuffer(mapVertices);
        IndexBuffer ib = new IndexBuffer(mapIndexes);
        vaMap.setIndexBuffer(ib);
        VertexBufferLayout layout = new VertexBufferLayout();
        layout.addFloat(2); //vertex coords
        layout.addFloat(2); //texture coords
        vaMap.addVertexBuffer(vb, layout);
    }

    private float[] quad(int x, int y, Color c, int id) {
        int size = 60;
        int half = size / 2;

        return new float[]{
                x - half, y - half, 0, 1, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), id,
                x + half, y - half, 1, 1, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), id,
                x + half, y + half, 1, 0, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), id,
                x - half, y + half, 0, 0, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), id,
        };
    }

    private void vertexHeroes() {
        heroLocs = Bootloader.loadMapCoords();
        vaHero = new VertexArray[heroLocs.length];
        for (int h = 0; h < heroLocs.length; h++) {
            ArrayList<HeroLocations> list = heroLocs[h];
            int spots = list.size();
            int[] heroIndexes = new int[spots * 6];
            for (int i = 0; i < spots; i++) {
                heroIndexes[i * 6] = i * 4;
                heroIndexes[i * 6 + 1] = 1 + i * 4;
                heroIndexes[i * 6 + 2] = 2 + i * 4;
                heroIndexes[i * 6 + 3] = 2 + i * 4;
                heroIndexes[i * 6 + 4] = 3 + i * 4;
                heroIndexes[i * 6 + 5] = i * 4;
            }

            float[] heroVertices = new float[spots * 4 * 9];

            for (HeroLocations hl : list) {
                addHeroToArray(heroVertices, hl);
            }

            VertexArray va = new VertexArray();
            VertexBuffer vb = new VertexBuffer(heroVertices, true);
            IndexBuffer ib = new IndexBuffer(heroIndexes);
            va.setIndexBuffer(ib);
            VertexBufferLayout layout = new VertexBufferLayout();
            layout.addFloat(2); //vertex coords
            layout.addFloat(2); //texture coords
            layout.addFloat(4); //color
            layout.addFloat(1); //id
            va.addVertexBuffer(vb, layout);
            vaHero[h] = va;
        }
    }

    private void addHeroToArray(float[] heroVertices, HeroLocations hero) {
        int x = hero.getX() - 1024;
        int y = 1024 - hero.getY();
        Color c = hero.getColor();
        int id = hero.getHeroTypeId();
        int heroIndex = hero.getIndex();
        float[] q = quad(x, y, c, id);
        if(q.length * (heroIndex + 1) < heroVertices.length) {
            System.arraycopy(q, 0, heroVertices, q.length * heroIndex, q.length);
        }
    }

    private void setupShaders() {
        shaderMap = new Shader("shader/map.vert", "shader/map.frag");
        shaderMap.bind();
        shaderMap.setUniform1i("uTexImage", 0);

        shaderHero = new Shader("shader/hero.vert", "shader/hero.frag");
        shaderHero.bind();
        shaderHero.setUniform1i("uTexImage", 0);
    }

    private void setupTextures() {
        BufferedImage[] maps = Bootloader.loadMaps();
        textureMaps = new Texture[maps.length];
        for (int i = 0; i < maps.length; i++) {
            int w = maps[i].getWidth();
            int h = maps[i].getHeight();
            BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            b.getGraphics().drawImage(maps[i], 0, 0, null);
            textureMaps[i] = new Texture(b, false);
        }

        BufferedImage[] heroes = Bootloader.loadHeroIcons();
        BufferedImage heroImages = new BufferedImage(72 * 13, 72, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < heroes.length; i++) {
            BufferedImage image = heroes[i];
            heroImages.getGraphics().drawImage(image, i * 72, 0, null);
        }
        textureHeroes = new Texture(heroImages, true);
    }

    private void preRender() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_COLOR);
        glEnable(GL_BLEND);

        renderer = new GLRenderer();

        proj = new Matrix4f();
        proj.ortho(-1024f * ratio, 1024f * ratio, -1024f, 1024f, -1.0f, 1.0f); // x*h/w or y*w/h

        mvp = proj;

        shaderMap.bind();
        shaderMap.setUniformMat4f("uMVP", mvp);
        shaderHero.bind();
        shaderHero.setUniformMat4f("uMVP", mvp);
    }

    GLRenderer fontRender;
    GLRenderer staticFontRender;
    GLRenderer staticFontRender2;
    GLFont font;
    GLFont fontAB10;
    GLFont fontAB20;

    private void font() {
        try {
            font = new GLFont(new FileInputStream("assets/font/Inconsolata.ttf"), 36);
            fontAB10 = new GLFont(new FileInputStream("assets/font/ariblk.ttf"), 10);
            fontAB20 = new GLFont(new FileInputStream("assets/font/ariblk.ttf"), 20);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        fontRender = new GLRenderer("font");
        staticFontRender = new GLRenderer("font");
        staticFontRender2 = new GLRenderer("font");

        Matrix4f proj = new Matrix4f();
        proj.ortho(-1024f * ratio, 1024f * ratio, -1024f, 1024f, -1.0f, 1.0f); // x*h/w or y*w/h

        fontRender.setMVP(proj);
    }

    private void render() {
        do {
//            fps();

            renderer.clear();

            if (viewChanged) {
                glViewport(0, 0, width, height);
                viewChanged = false;

                Matrix4f proj = new Matrix4f();
                proj.ortho(0, width, 0, height, -1.0f, 1.0f); // x*h/w or y*w/h
                staticFontRender.setMVP(proj);
                staticFontRender2.setMVP(proj);
            }

            if (refresh) {
                shaderMap.bind();
                shaderMap.setUniformMat4f("uMVP", mvp);
                shaderHero.bind();
                shaderHero.setUniformMat4f("uMVP", mvp);
                fontRender.setMVP(mvp);

                vaHero[mapIndex].updateDynamicVertexBufferFloats();

                refresh = false;
            }
//
            if (showMap) {
                textureMaps[mapIndex].bind(0);
                renderer.draw(vaMap, vaMap.getIndexBuffer(), shaderMap);
            }

            if (showHeroes && model.inRealm()) {
                font.drawHeroTexts(fontRender, heroLocs[mapIndex]);
                textureHeroes.bind(0);
                renderer.draw(vaHero[mapIndex], vaHero[mapIndex].getIndexBuffer(), shaderHero);
//                g.setFont(new Font("Monospaced", Font.PLAIN, model.getFontSize()));
            }

            if (userShowInfo) {
                if (model.renderCastleTimer()) {
//////                    g.setFont(new Font("Arial Black", Font.PLAIN, 20));
//////                    g.drawString(model.getCastleTimer(), 5, 20);
                    fontAB20.drawText(staticFontRender, model.getCastleTimer(), 5, height - 30);
                } else if (showHeroCount) {
//////                    g.setFont(new Font("Arial Black", Font.PLAIN, 20));
//////                    g.drawString(String.format("(%d) Heroes:%d", model.getMapIndex() + 1, model.getHeroesLeft()), 5, 20);
                    String h = String.format("(%d) Heroes:%d", mapIndex + 1, model.getHeroesLeft());
                    fontAB20.drawText(staticFontRender, h, 5, height - 30);

                    String s = String.format("x:%d y:%d  %s  %s  %s", model.getIntPlayerX(), model.getIntPlayerY(), model.getServerName(), model.getRealmName(), model.getTpCooldown());
                    fontAB10.drawText(staticFontRender2, s, 5, 5);
                }
//
//                g.setFont(new Font("Arial Black", Font.PLAIN, 10));
//                g.drawString(, 5, model.getFrameHeight() - 5);
//                renderText(String.format("(%d) Heroes:%d", mapIndex + 1, model.getHeroesLeft()) );

            }

            glfwSwapBuffers(window); // Update Window
            glfwPollEvents(); // Key Mouse Input
        } while (running && !glfwWindowShouldClose(window));

        glfwTerminate();
    }

    public void setWindow(int x, int y, int w, int h) {
        width = w;
        height = h;
        ratio = (float) width / height;
//        gluOrtho2D(left * ratio, right * ratio, bottom, top);
        glfwSetWindowPos(window, x, y);
        glfwSetWindowSize(window, w, h);
        proj = new Matrix4f();
        proj.ortho(-1024f * ratio, 1024f * ratio, -1024f, 1024f, -1.0f, 1.0f); // x*h/w or y*w/h
        mvp = proj;
        viewChanged = true;
    }

    public void show() {
        glfwShowWindow(window);
    }

    public void hide() {
        glfwHideWindow(window);
    }

    public void renderMap(boolean b) {
        showMap = b;
        showHeroes = b && userShowHeroes;
        showMap = b && userShowMap;
        showHeroCount = b;
    }

    public static void showMap(boolean show) {
        userShowMap = show;
        showMap = show;
        System.out.println("Show map: " + show);
    }

    public static void showHeroes(boolean show) {
        userShowHeroes = show;
        showHeroes = show;
        System.out.println("Show heroes: " + show);
    }

    public static void showInfo(boolean show) {
        userShowInfo = show;
        System.out.println("Show info: " + show);
    }

    public void setCamera(float x, float y, int zoom) {
        this.zoom = zoom;
        Matrix4f view = new Matrix4f();
        refresh = true;
        mvp = view.translate(playerOffset[zoom] * (1024f - x) / 2048f, ratio * playerOffset[zoom] * (y - 1024f) / 2048f, 0).scale(scale[zoom]).mul(proj);
    }

    public void setMap(int mapIndex) {
        this.mapIndex = mapIndex;
        refresh = true;
        System.out.println("mapIndex " + mapIndex);
    }

    public void updateHero(HeroLocations hero) {
        float[] floats = vaHero[mapIndex].getDynamicVertexBufferFloats();
        addHeroToArray(floats, hero);
        refresh = true;
    }

    public void dispose() {
        running = false;
        shaderMap.dispose();
        shaderHero.dispose();
        vaMap.dispose();
        for (VertexArray hero : vaHero) {
            hero.dispose();
        }
        for (Texture textureMap : textureMaps) {
            textureMap.dispose();
        }
        textureHeroes.dispose();
    }

    public static class ImageParser {
        private final ByteBuffer image;
        private final int width, heigh;

        public ByteBuffer getImage() {
            return image;
        }

        public int getWidth() {
            return width;
        }

        public int getHeigh() {
            return heigh;
        }

        ImageParser(int width, int heigh, ByteBuffer image) {
            this.image = image;
            this.heigh = heigh;
            this.width = width;
        }

        public ImageParser loadImage(String path) {
            ByteBuffer image;
            int width, heigh;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer comp = stack.mallocInt(1);
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);

                image = stbi_load(path, w, h, comp, 4);
                if (image == null) {
                    throw new RuntimeException("Could not load image resources.");
                }
                width = w.get();
                heigh = h.get();
            }
            return new ImageParser(width, heigh, image);
        }
    }
}
