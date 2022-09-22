package potato.view;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import potato.model.Bootloader;
import potato.model.HeroLocations;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class RenderViewer {
    private static int width = 300;
    private static int height = 300;
    private final JWindow frame;
    private final JPanel panel;
    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
    private Composite originalComposite;

    private final int[] zooms = {0, 70, 172, 340, 670, 1560, 15300};
    private final float[] m = {0, 0.0294f, 0.072f, 0.1447f, 0.286f, 2f / 3f, 6.66f};
    private final int[] k = {0, 6, 12, 22, 44, 100, 820};
    private final int[] circleSize = {5, 7, 8, 9, 10, 16, 130};
    private final int[] fontSize = {0, 8, 8, 8, 10, 16, 130};

    public static final int[] imageSize = {308, 366, 452, 600, 925, 1725, 14788};
    public static final float[] imageM = {0, -0.03f, -0.071f, -0.144f, -0.3f, -0.691f, -7.222f};
    public static final int[] imageK = {22, 24, 24, 24, 24, 20, 175};
    public static int imageOffsetX = 0;
    public static int imageOffsetY = 0;

    private static int offsetX = 0;
    private static int offsetY = 0;
    public static int zoom;
    private final BufferedImage[] images;
    private final ArrayList<HeroLocations>[] mapCoords;
    private int mapIndex = 0;
    public static int playerX;
    public static int playerY;
    private int heroesLeft;
    private boolean inRealm = true;
    private String castleTimer = "";
    private static boolean userShowMap = true;
    private static boolean userShowHeroes = true;
    private static boolean userShowCoords = true;
    private static boolean showMap = false;
    private static boolean showHeroes = false;
    private boolean showHeroCount = false;
    private boolean startCastleTimer = false;
    private long realmClosingTime = 0;
    private String serverName = "";
    private String realmName = "";
    private long tpCooldown;

    private boolean running;
    private final Image[] heroIcon;

    public RenderViewer(ArrayList<HeroLocations>[] mapCoords) {
        this.mapCoords = mapCoords;
        images = Bootloader.loadMaps();
        heroIcon = Bootloader.loadHeroIcons();

        frame = new JWindow();
        frame.setAlwaysOnTop(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                render((Graphics2D) g);
            }
        };
        panel.setOpaque(false);
        frame.add(panel);
        frame.setSize(1, 1);
        frame.setLocation(-1, -1);
        frame.setVisible(true);
        setTransparent(frame);
    }

    private void setTransparent(Component w) {
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(w));
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    public void render(Graphics2D g) {
        if (originalComposite == null) originalComposite = g.getComposite();
        if (showMap) {
            g.setComposite(composite);
            g.drawImage(images[mapIndex], imageOffsetX, imageOffsetY, imageSize[zoom], imageSize[zoom], null);
            g.setComposite(originalComposite);
        }
        if (showHeroes && inRealm) {
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Monospaced", Font.PLAIN, fontSize[zoom]));
            for (HeroLocations h : mapCoords[mapIndex]) {
                drawHeroes(g, h);
            }
            if (startCastleTimer) {
                g.setColor(Color.lightGray);
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(castleTimer, 5, 20);
            } else if (showHeroCount) {
                g.setColor(Color.lightGray);
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(String.format("(%d) Heroes:%d", mapIndex + 1, heroesLeft), 5, 20);
            }
        }
        if (userShowCoords) {
            g.setColor(Color.lightGray);
            g.setFont(new Font("Arial Black", Font.PLAIN, 10));
            g.drawString(String.format("x:%d y:%d  %s  %s  %s", playerX, playerY, serverName, realmName, cooldown()), 5, height - 5);
        }
        g.dispose();
    }

    private String cooldown() {
        if (tpCooldown == 0) return "";
        long t = (tpCooldown - System.currentTimeMillis()) / 1000;
        if (t < 0) {
            tpCooldown = 0;
            return "";
        }
        return String.format("(tp:%ds)", t);
    }

    public void drawHeroes(Graphics2D g, HeroLocations h) {
        int drawIndex = h.getDrawIndexNum();
        if (drawIndex < 0) {
            g.setColor(h.getColor());
            g.setComposite(composite);
            g.drawOval(h.getDrawX() - circleSize[zoom], h.getDrawY() - circleSize[zoom], circleSize[zoom] * 2, circleSize[zoom] * 2);
            g.setComposite(originalComposite);
            setTextCenter(g, h.getIndexString(), h.getDrawX(), h.getDrawY());
        } else {
            g.drawImage(heroIcon[drawIndex], h.getDrawX() - circleSize[zoom], h.getDrawY() - circleSize[zoom], circleSize[zoom] * 2, circleSize[zoom] * 2, null);
            setTextCenter(g, h.getIndexString(), h.getDrawX(), h.getDrawY());
        }
    }

    private void setTextCenter(Graphics2D g, String string, int x, int y) {
        if (fontSize[zoom] == 0) return;
        int stringWidthLength = (int) g.getFontMetrics().getStringBounds(string, g).getWidth();
        int stringHeightLength = (int) g.getFontMetrics().getStringBounds(string, g).getHeight();

        int horizontalCenter = x - stringWidthLength / 2;
        int verticalCenter = y + stringHeightLength / 4;
        g.drawString(string, horizontalCenter, verticalCenter);
    }

    public void stuffRender(boolean b) {
        showHeroes = b && userShowHeroes;
        showMap = b && userShowMap;
        showHeroCount = b;
    }

    public void renderLoop() {
        new Thread(() -> {
            running = true;
            while (running) {
                try {
                    panel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void calcCoords() {
//        System.out.printf("offset x:%d y:%d\n", offsetX, offsetY);
        int sW = width + zooms[zoom];
        int sH = height + zooms[zoom];
        for (HeroLocations h : mapCoords[mapIndex]) {
            double dx = ((float) (h.getX() + 150) / 2350);
            double dy = ((float) (h.getY() + 150) / 2350);
            h.setDrawX((int) (dx * sW) - offsetX);
            h.setDrawY((int) (dy * sH) - offsetY);
        }
    }

    public void setPlayerCoords(int x, int y) {
        offsetX = (int) (m[zoom] * x + k[zoom]);
        offsetY = (int) (m[zoom] * y + k[zoom]);
        imageOffsetX = (int) (imageM[zoom] * x + imageK[zoom]);
        imageOffsetY = (int) (imageM[zoom] * y + imageK[zoom]) + 4;
        playerX = x;
        playerY = y;
        calcCoords();
    }

    public void setSizeLoc(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        frame.setLocation(x, y);
        frame.setSize(width, height);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void remove() {
        frame.setVisible(false);
    }

    public void setInRealm(boolean b) {
        inRealm = b;
    }

    public void setHeroesLeft(int i) {
        heroesLeft = i;
    }

    public void setServerTime(long serverTime) {
        if (startCastleTimer) {
            long timeSense = serverTime - realmClosingTime;
            int remTime = (int) ((130000 - timeSense) / 1000);
            if (remTime <= 0) {
                startCastleTimer = false;
                return;
            }
//                System.out.println(timeSense + " " + remTime);
            castleTimer = String.format("Castle %d:%02d", remTime / 60, remTime % 60);
        }
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

    public static void showCoords(boolean show) {
        userShowCoords = show;
        System.out.println("Show coords: " + show);
    }

    public void setZoom(int i, int x, int y) {
        this.zoom = i;
        imageOffsetX = (int) (imageM[zoom] * playerX + imageK[zoom]);
        imageOffsetY = (int) (imageM[zoom] * playerY + imageK[zoom]) + 4;
        offsetX = (int) (m[zoom] * x + k[zoom]);
        offsetY = (int) (m[zoom] * y + k[zoom]);
        calcCoords();
    }

    public void editMapIndex(int i) {
        mapIndex = i;
        calcCoords();
    }

    public void realmClosed(long time) {
        startCastleTimer = true;
        realmClosingTime = time;
    }

    public void serverNameAndCooldown(String name) {
        if (!serverName.equals("")) tpCooldown = System.currentTimeMillis() + 125000;
        serverName = name;
    }

    public void realmName(String name) {
        int i = name.indexOf('.');
        if (i > 0) {
            realmName = name.substring(i + 1);
        } else {
            realmName = name;
        }
    }
}
