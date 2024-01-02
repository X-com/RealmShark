package potato.view.opengl;

import io.github.chiraagchakravarthy.lwjgl_vectorized_text.TextRenderer;
import io.github.chiraagchakravarthy.lwjgl_vectorized_text.VectorFont;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import potato.model.Config;
import potato.model.DataModel;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class OpenGLPotato extends Thread {

    private static OpenGLPotato instance;
    public boolean waitfor = true;
    private final DataModel model;
    private boolean running;

    private Matrix4f mvp;
    private int backgroundChange;
    private int mapIndex = 0;

    private boolean firstDisplay = true;
    private static boolean userShowMap = true;
    private static boolean userShowHeroes = true;
    private static boolean userShowInfo = true;
    private static boolean showMap = false;
    private static boolean showHeroes = false;
    private boolean showHeroCount = false;
    public static boolean refresh = false;

    private static WindowGLFW window;
    private static GLMapRender renderMap;
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
        window = new WindowGLFW();

        renderMap = new GLMapRender();
        //vertexMap();

        // ----


        // setupShaders();

        // ----


        // setupTextures();

        // ----

        // preRender();
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_COLOR);
//        glEnable(GL_BLEND);

//        mvp = new Matrix4f();
//
//        shaderMap.bind();
//        shaderMap.setUniformMat4f("uMVP", mvp);

        heroes = new GLHeroes();
        // ----

        // font();
        vectorFont = new VectorFont("/font/ariblk.ttf");
        vectorShapes = new VectorFont("/font/shapes.ttf");
        renderHud = new TextRenderer(vectorFont);
        renderText = new TextRenderer(vectorFont);
        renderShape = new TextRenderer(vectorShapes);
        // ----

        waitfor = false;
        running = true;

        do {
            window.checkViewChange();

            tick();

            window.swapBuffer();
        } while (running && !window.shouldWindowClose());

        glfwTerminate();
    }

    private void tick() {
        //            fps();

        renderMap.clear();

        if (backgroundChange != 0) {
            clearColors(backgroundChange);
            backgroundChange = 0;
        }

//        if (refresh) {
//            shaderMap.bind();
//            shaderMap.setUniformMat4f("uMVP", mvp);
//            shaderMap.setUniform1f("alpha", mapAlpha / 255f);
//
//            refresh = false;
//        }

        if (showMap && userShowMap && model.inRealm() && zoom != 1) {
            renderMap.draw();
        }

        if (showHeroes && userShowHeroes && model.inRealm() && zoom != 1) {
            heroes.drawHeros(model.mapHeroes(), mvp, mapSize);
            heroes.drawShapes(model.mapEntitys(), mvp, mapSize);
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
                String h = String.format("[%d] Heroes:%d %s%s", mapIndex + 1, model.getHeroesLeft(), !model.isServerOnline ? " Offline" : "", Config.instance.saveMapInfo ? " R" : "");
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

    public void renderMap(boolean b) {
        showMap = b;
        showHeroes = b;
        showHeroCount = b;
    }

    public static void setMapAlpha(int alpha) {
        instance.mapAlpha(alpha);
    }

    private void mapAlpha(int alpha) {
        renderMap.setAlpha(alpha);
//        refresh = true;
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
        if (renderMap != null) {
            renderMap.setMVP(mvp);
        }
    }

    public void setMap(int mapIndex) {
        this.mapIndex = mapIndex;
        renderMap.setMap(mapIndex);
        refresh = true;
    }

    public void dispose() {
        running = false;
        renderMap.dispose();
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

    public void toggleShowAll() {
        if (window != null) {
            window.toggleShowAll();
        }
    }
}
