package potato.view;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import potato.model.Bootloader;
import potato.model.HeroLocations;
import tomato.Tomato;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

public class RenderViewer {
    int width = 300;
    int height = 300;
    JFrame menuFrame;
    JWindow frame;
    JPanel panel;
    AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
    static public URL imagePath = Tomato.class.getResource("/icon/potatoIcon.png");
    static private Image icon = Toolkit.getDefaultToolkit().getImage(imagePath);

    private final int[] zooms = {0, 70, 172, 340, 670, 1560, 15300};
    private final float[] m = {0, 0.02949208f, 0.0720917f, 0.1447f, 0.286f, 2f / 3f, 6.66f};
    private final int[] k = {0, 6, 12, 22, 44, 100, 820};
    private final int[] circleSize = {5, 7, 8, 9, 10, 16, 130};
    private final int[] fontSize = {0, 8, 8, 8, 10, 16, 130};
    private int offsetX = 0;
    private int offsetY = 0;
    private int zoom;
    private BufferedImage[] images;
    private ArrayList<HeroLocations>[] mapCoords;
    private int mapIndex = 0;
    private int playerX;
    private int playerY;
    private int heroesLeft;
    private boolean inRealm = true;
    private String castleTimer = "";
    private boolean toggleMap = false;
    private boolean toggleDots = true;
    private boolean showMap = false;
    private boolean showHeroes = true;
    private boolean showDots = true;
    private boolean startCastleTimer = false;
    private long realmClosingTime = 0;

    private boolean running;
    private Image[] heroIcon;

    public RenderViewer(ArrayList<HeroLocations>[] mapCoords) {
        this.mapCoords = mapCoords;
        images = Bootloader.loadMaps();
        heroIcon = Bootloader.loadHeroIcons();
        smallWindow();

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

    private void smallWindow() {
        menuFrame = new JFrame("Potato") {
            @Override
            public void dispose() {
                super.dispose();
                System.exit(0);
            }
        };
        menuFrame.setIconImage(icon);
        menuFrame.setSize(100, 100);
        menuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        menuFrame.setVisible(true);
    }

    private void setTransparent(Component w) {
        WinDef.HWND hwnd = getHWnd(w);
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    private WinDef.HWND getHWnd(Component w) {
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(w));
        return hwnd;
    }

    public void render(Graphics2D g) {
        if (showMap) {
            Composite originalComposite = g.getComposite();
            g.setComposite(composite);
            g.drawImage(images[mapIndex], -offsetX, -offsetY, width + zooms[zoom], height + zooms[zoom], null);
            g.setComposite(originalComposite);
        }
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Monospaced", Font.PLAIN, fontSize[zoom]));
        if (showDots && inRealm) {
            for (HeroLocations h : mapCoords[mapIndex]) {
                drawHeroes(g, h);
            }
        }
        g.setColor(Color.lightGray);
        if (startCastleTimer) {
            g.setFont(new Font("Arial Black", Font.PLAIN, 20));
            g.drawString(castleTimer, 5, 20);
        } else if (showHeroes) {
            g.setFont(new Font("Arial Black", Font.PLAIN, 20));
            g.drawString(String.format("(%d) Heroes:%d", mapIndex + 1, heroesLeft), 5, 20);
        }
        g.setFont(new Font("Arial Black", Font.PLAIN, 10));
        g.drawString(String.format("x:%d y:%d", playerX, playerY), 5, height - 5);
        g.dispose();
    }

    public void drawHeroes(Graphics2D g, HeroLocations h) {
        int drawIndex = h.getDrawIndexNum();
        if (drawIndex < 0) {
            g.setColor(h.getColor());
            g.drawOval(h.getDrawX() - circleSize[zoom], h.getDrawY() - circleSize[zoom], circleSize[zoom] * 2, circleSize[zoom] * 2);
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
        showDots = b && toggleDots;
        showMap = b && toggleMap;
        showHeroes = b;
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

    public void toggleMap() {
        toggleMap = !toggleMap;
        showMap = toggleMap;
        System.out.println("Toggle map: " + toggleMap);
    }

    public void toggleDots() {
        toggleDots = !toggleDots;
        showDots = toggleDots;
        System.out.println("Toggle dots: " + toggleDots);
    }

    public void setZoom(int i, int x, int y) {
        this.zoom = i;
        offsetX = (int) (m[zoom] * x + k[zoom]);
        offsetY = (int) (m[zoom] * y + k[zoom]);
        calcCoords();
    }

    public void editMapIndex(int i) {
        mapIndex = i;
        calcCoords();
    }
}
