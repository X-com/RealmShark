package potato.view.opengl;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import io.github.chiraagchakravarthy.lwjgl_vectorized_text.VectorFont;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import potato.model.Bootloader;
import potato.model.Config;
import potato.model.DataModel;

import java.awt.image.BufferedImage;

import static com.sun.jna.platform.win32.WinUser.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.windows.User32.WS_EX_APPWINDOW;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

public class OpenGLPotato extends Thread {

    private static OpenGLPotato instance;
    public boolean waitfor = true;
    private final DataModel model;
    private boolean running;

    private long window;
    private HWND hwnd;
    private Shader shaderMap;
    private VertexArray vaMap;
    private Texture[] textureMaps;
    private Matrix4f mvp;
    public static Matrix4f proj;
    private boolean viewChanged;
    private int backgroundChange;
    private int mapIndex = 0;

    private int mapAlpha = 150;
//    public static float ratio;

    private boolean firstDisplay = true;
    private static boolean userShowAll = true;
    private static boolean userShowMap = true;
    private static boolean userShowHeroes = true;
    private static boolean userShowInfo = true;
    private static boolean showMap = false;
    private static boolean showHeroes = false;
    private boolean showHeroCount = false;
    public static boolean refresh = false;

    public static VectorFont vectorFont;
    public static VectorFont vectorShapes;
    public static TextRenderer renderHud;
    public static TextRenderer renderText;
    public static TextRenderer renderShape;
    public static Vector2f bottomLeftVec = new Vector2f(-1, -1);

    private GLHeroes heroes;

    // temp //
    private final Vector4f mainTextColor = new Vector4f(0.75f, 0.75f, 0.75f, 1.0f);

    private int mapSize = 2048;
    private float zoomMax = 48f;
    private float xLeft = -128f;
    private float yBot = 2176f;
    private Matrix4f view = new Matrix4f();
    private float zoom;

//    long time;
//    private void fps() {
//        long now = System.nanoTime();
//        System.out.println((now - time) / 1000000f);
//        time = now;
//    }
    // temp end //

    public OpenGLPotato(DataModel dataModel) {
        instance = this;
        this.model = dataModel;
    }

    public void run() {
        makeWindow();
        vertexMap();
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
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        window = glfwCreateWindow(Config.instance.mapWidth, Config.instance.mapHeight, "Potato", NULL, NULL);
        glfwSetWindowPos(window, Config.instance.mapTopLeftX, Config.instance.mapTopLeftY);
        hwnd = new HWND(new Pointer(glfwGetWin32Window(window)));
        hideTaskBarIcon();

//        ratio = (float) Config.instance.mapWidth / Config.instance.mapHeight;
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to open GLFW window. If you have an Intel GPU, they are not 3.3 compatible. Try the 2.1 version of the tutorials.");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();
        glfwShowWindow(window);
        System.out.println("Using GL Version: " + glGetString(GL_VERSION));

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void hideTaskBarIcon() {
        int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        style &= ~(WS_VISIBLE);    // this works - window become invisible

        style |= WS_EX_TOOLWINDOW;   // flags don't work - windows remains in taskbar
        style &= ~(WS_EX_APPWINDOW);

        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, style);
    }

    private void vertexMap() {
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
    }

    private void setupShaders() {
        shaderMap = new Shader("shader/map.vert", "shader/map.frag");
        shaderMap.bind();
        shaderMap.setUniform1i("uTexImage", 0);
        shaderMap.setUniform1f("alpha", Config.instance.mapTransparency / 255f);
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
    }

    private void preRender() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_COLOR);
        glEnable(GL_BLEND);

//        renderer = new GLRenderer();

        proj = new Matrix4f();
//        proj.ortho(-1024f, 1024f, -1024f / ratio, 1024f / ratio, -1.0f, 1.0f); // x*h/w or y*w/h

        mvp = proj;

        shaderMap.bind();
        shaderMap.setUniformMat4f("uMVP", mvp);

        heroes = new GLHeroes();
    }

    private void font() {
        vectorFont = new VectorFont("/font/ariblk.ttf");
        vectorShapes = new VectorFont("/font/shapes.ttf");
        renderHud = new TextRenderer(vectorFont);
        renderText = new TextRenderer(vectorFont);
        renderShape = new TextRenderer(vectorShapes);
    }

    private void setWindow() {
//        ratio = (float) Config.instance.mapWidth / Config.instance.mapHeight;
        glfwSetWindowPos(window, Config.instance.mapTopLeftX, Config.instance.mapTopLeftY);
        glfwSetWindowSize(window, Config.instance.mapWidth, Config.instance.mapHeight);
//        proj = new Matrix4f();
//        System.out.println(ratio);
//        proj.ortho(-1024f, 1024f, -1024f / ratio, 1024f / ratio, -1.0f, 1.0f); // x*h/w or y*w/h
//        mvp = proj;
        glViewport(0, 0, Config.instance.mapWidth, Config.instance.mapHeight);
    }

    private void render() {
        do {
//            fps();

            GLRenderer.clear();

            if (backgroundChange != 0) {
                clearColors(backgroundChange);
                backgroundChange = 0;
            }

            if (viewChanged) {
                setWindow();
                viewChanged = false;
            }

            if (refresh) {
                shaderMap.bind();
                shaderMap.setUniformMat4f("uMVP", mvp);
                shaderMap.setUniform1f("alpha", mapAlpha / 255f);

                refresh = false;
            }

            if (showMap && userShowMap && model.inRealm() && zoom != 1) {
                textureMaps[mapIndex].bind(0);
                GLRenderer.draw(vaMap, vaMap.getIndexBuffer(), shaderMap);
            }

//            if (showHeroes && userShowHeroes && model.inRealm() && zoom != 6) {
            if (showHeroes && userShowHeroes && model.inRealm() && zoom != 1) {
                heroes.drawHeros(model.mapHeroes(), mvp, mapSize);
            } else if (showHeroes && userShowHeroes && model.isShatters() && zoom != 1) {
                firstDisplay = false;
                heroes.drawShapes(model.mapEntitys(), mvp, mapSize);
            } else if (showHeroes && userShowHeroes && model.isCrystal() && zoom != 1) {
                firstDisplay = false;
                heroes.drawCrystal(model.mapEntitys(), model.playerX, model.playerY, mvp, mapSize);
            }

            if (firstDisplay && !model.inRealm()) {
                renderHud.drawText2D("Enter any realm or re-enter if starting in a realm.", 5, 3, 10, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
            } else if (userShowInfo && (Config.instance.alwaysShowCoords || model.inRealm())) {
                firstDisplay = false;
                if (model.renderCastleTimer()) {
                    String s = String.format("%s%s", model.getCastleTimer(), Config.instance.saveMapInfo ? " R" : "");
                    renderHud.drawText2D(s, 5, Config.instance.mapHeight - 20, 20, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
                } else if (showHeroCount) {
                    String h = String.format("[%d] Heroes:%d %s", mapIndex + 1, model.getHeroesLeft(), Config.instance.saveMapInfo ? " R" : "");
                    renderHud.drawText2D(h, 5, Config.instance.mapHeight - 23, 20, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
                } else if (Config.instance.saveMapInfo) {
                    renderHud.drawText2D("R", 5, Config.instance.mapHeight - 23, 20, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
                }
                String s = String.format("%s%s %s %s %s", model.getDungeonTime(), model.getPlayerCoordString(), model.getServerName(), model.getRealmName(), model.extraInfo());
                renderHud.drawText2D(s, 5, 3, 10, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
            } else if (Config.instance.saveMapInfo || model.renderCastleTimer()) {
                String s = String.format("%s%s", model.getCastleTimer(), Config.instance.saveMapInfo ? " R" : "");
                renderHud.drawText2D(s, 5, Config.instance.mapHeight - 23, 20, bottomLeftVec, TextRenderer.TextBoundType.BOUNDING_BOX, mainTextColor);
            }
            renderHud.render();
            renderText.render();
            renderShape.render();

            glfwSwapBuffers(window); // Update Window
            glfwPollEvents(); // Key Mouse Input
        } while (running && !glfwWindowShouldClose(window));

        glfwTerminate();
    }

    public static void viewChanged() {
        instance.viewChanged = true;
    }

    public static void setColor(int color) {
        instance.backgroundChange = color;
    }

    private void clearColors(int color) {
        if (color == 1) {
            glClearColor(0.75f, 0.75f, 0.75f, 0.75f);
        } else {
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    public void show() {
        try {
            glfwShowWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        try {
            glfwHideWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderMap(boolean b) {
        showMap = b;
        showHeroes = b;
        showHeroCount = b;
    }

    public static void setMapAlpha(int alpha) {
        instance.mapAlpha(alpha);
    }

    private void mapAlpha(int alpha) {
        mapAlpha = alpha;
        refresh = true;
    }

    public static void toggleShowAll() {
        if (userShowAll) {
            instance.hide();
            userShowAll = false;
        } else {
            instance.show();
            userShowAll = true;
        }
    }

    public static void toggleShowMap() {
        showMap(!userShowMap);
    }

    public static void toggleShowHeroes() {
        showHeroes(!userShowHeroes);
    }

    public static void toggleShowInfo() {
        showInfo(!userShowInfo);
    }

    public static void showMap(boolean show) {
        userShowMap = show;
        System.out.println("Show map: " + show);
    }

    public static void showHeroes(boolean show) {
        userShowHeroes = show;
        System.out.println("Show heroes: " + show);
    }

    public static void showInfo(boolean show) {
        userShowInfo = show;
        System.out.println("Show info: " + show);
    }

    public void setCamera(float x, float y, float zoom) {
        this.zoom = zoom;
        refresh = true;
        y = mapSize - y;
        view.identity();
        if (zoom == 1f) {
            float xmin = x - 24.0f;
            float xmax = xmin + 48f;
            float ymax = y + 24.0f;
            float ymin = ymax - 48f;
            mvp = view.ortho(xmin, xmax, ymin, ymax, -1.0f, 1.0f);
        } else {
            float xmin = xLeft * zoom / zoomMax - x * zoom / zoomMax + x;
            float xmax = xmin + 48f * zoom;
            float ymax = yBot * zoom / zoomMax - y * zoom / zoomMax + y;
            float ymin = ymax - 48f * zoom;
            mvp = view.ortho(xmin, xmax, ymin, ymax, -1.0f, 1.0f);
        }
    }

    public void setMap(int mapIndex) {
        this.mapIndex = mapIndex;
        refresh = true;
    }

    public void dispose() {
        running = false;
        shaderMap.dispose();
        vaMap.dispose();
        for (Texture textureMap : textureMaps) {
            textureMap.dispose();
        }
    }

    public void mapSize(int mapSize) {
        this.mapSize = mapSize;
        if (mapSize == 2048) {
            zoomMax = 48f;
            xLeft = -128f;
            yBot = 2176f;
        } else if (mapSize == 512) {
            zoomMax = 12f;
            xLeft = -32f;
            yBot = 544f;
        } else if (mapSize == 256) {
            zoomMax = 6f;
            xLeft = -16f;
            yBot = 272f;
        } else if (mapSize == 128) {
            zoomMax = 3f;
            xLeft = -8f;
            yBot = 136f;
        }
    }
}
