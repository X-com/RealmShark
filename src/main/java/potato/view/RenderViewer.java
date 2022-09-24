package potato.view;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import potato.model.DataModel;
import potato.model.HeroLocations;

import javax.swing.*;
import java.awt.*;

public class RenderViewer {

    private final DataModel model;

    private final JWindow frame;
    private final JPanel panel;
    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
    private Composite originalComposite;

    private static boolean userShowMap = true;
    private static boolean userShowHeroes = true;
    private static boolean userShowInfo = true;
    private static boolean showMap = false;
    private static boolean showHeroes = false;
    private boolean showHeroCount = false;

    private boolean running;

    public RenderViewer(DataModel model) {
        this.model = model;

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
            g.drawImage(model.getMapImage(), model.getImageOffsetX(), model.getImageOffsetY(), model.getImageSize(), model.getImageSize(), null);
            g.setComposite(originalComposite);
        }
        if (showHeroes && model.inRealm()) {
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Monospaced", Font.PLAIN, model.getFontSize()));
            for (HeroLocations h : model.getMapCoords()) {
                drawHeroes(g, h);
            }
        }
        if (userShowInfo) {
            g.setColor(Color.lightGray);
            if (model.renderCastleTimer()) {
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(model.getCastleTimer(), 5, 20);
            } else if (showHeroCount) {
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(String.format("(%d) Heroes:%d", model.getMapIndex() + 1, model.getHeroesLeft()), 5, 20);
            }
            g.setFont(new Font("Arial Black", Font.PLAIN, 10));
            g.drawString(String.format("x:%d y:%d  %s  %s  %s", model.getPlayerX(), model.getPlayerY(), model.getServerName(), model.getRealmName(), model.getTpCooldown()), 5, model.getFrameHeight() - 5);
        }
        g.dispose();
    }

    public void drawHeroes(Graphics2D g, HeroLocations h) {
        int drawIndex = h.getDrawIndexNum();
        if (drawIndex < 0) {
            g.setColor(h.getColor());
            g.setComposite(composite);
            g.drawOval(h.getDrawX() - model.getCircleSize(), h.getDrawY() - model.getCircleSize(), model.getCircleSize() * 2, model.getCircleSize() * 2);
            g.setComposite(originalComposite);
            setTextCenter(g, h.getIndexString(), h.getDrawX(), h.getDrawY());
        } else {
            g.drawImage(model.getHeroImage(drawIndex), h.getDrawX() - model.getCircleSize(), h.getDrawY() - model.getCircleSize(), model.getCircleSize() * 2, model.getCircleSize() * 2, null);
            setTextCenter(g, h.getIndexString(), h.getDrawX(), h.getDrawY());
        }
    }

    private void setTextCenter(Graphics2D g, String string, int x, int y) {
        if (model.getFontSize() == 0) return;
        int stringWidthLength = (int) g.getFontMetrics().getStringBounds(string, g).getWidth();
        int stringHeightLength = (int) g.getFontMetrics().getStringBounds(string, g).getHeight();

        int horizontalCenter = x - stringWidthLength / 2;
        int verticalCenter = y + stringHeightLength / 4;
        g.drawString(string, horizontalCenter, verticalCenter);
    }

    public void renderMap(boolean b) {
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

    public void setSizeLoc(int x, int y, int width, int height) {
        frame.setLocation(x, y);
        frame.setSize(width, height);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void remove() {
        frame.setVisible(false);
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

    public void dispose() {
        running = false;
    }
}
