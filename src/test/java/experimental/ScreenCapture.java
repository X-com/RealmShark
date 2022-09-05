package experimental;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenCapture extends Thread implements NativeMouseWheelListener {

    int width = 976, height = 579;
    int storeX, storeY, storeW, storeH;

    JPanel mainPanel;
    Window hollowgram;
    JFrame frame;
    Canvas asscan;

    public ScreenCapture() throws AWTException {
    }

    public static void main(String[] args) throws AWTException {
        disableLogger();

        ScreenCapture sc = new ScreenCapture();
//            sc.makeFrame();
        sc.run();
    }

    private static void disableLogger() {
        // Get the logger for "com.github.kwhat.jnativehook" and set the level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Don't forget to disable the parent handlers.
        logger.setUseParentHandlers(false);
    }

    Robot r = new Robot();
    Toolkit t = Toolkit.getDefaultToolkit();
    Dimension d = t.getScreenSize();
    Rectangle rectangle = new Rectangle(0, 0, d.width, d.height);

    public BufferedImage capture() {
        return r.createScreenCapture(rectangle);
    }

    public void run() {
        int frames = 0;
        int ticks = 0;

        long lastTimer = System.currentTimeMillis();
        boolean running = true;

        calcMapSizeLoc();

        enableTransparentWindow(0.4f);
//        createHollow();

        BufferStrategy strategy = asscan.getBufferStrategy();
        BufferedImage mapImg = createBackGroundImage();
        try {
            mouseThing();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        System.out.println("clearconsole");

        while (running) {
            frames++;
            if (strategy != null) {
                BufferedImage img = capture();
                Graphics2D draw = (Graphics2D) strategy.getDrawGraphics();
                draw.drawImage(mapImg, 0, 0, storeW, storeH, null);
                draw.dispose();
                strategy.show();
            }

//            Graphics g = frame.getGraphics();
//            hollowgram.update(g);
//            hollowgram.repaint();
//            asscan.update(hollowgram.getGraphics());

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
//                System.out.println(frames + " " + ticks);
                frames = 0;
                ticks = 0;
            }
        }
    }

    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
//        System.out.println("Mosue Wheel Moved: " + e.getWheelRotation());
        if (e.getWheelRotation() == 1) {

        } else if (e.getWheelRotation() == -1) {

        }
    }

    private void mouseThing() throws AWTException {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            System.exit(1);
        }

        GlobalScreen.addNativeMouseWheelListener(new ScreenCapture());
    }

    public void eventDispatched2(AWTEvent e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            System.out.println(me);
        }
    }

    private void calcMapSizeLoc() {
        int scan = -8553091;
        BufferedImage img = capture();
        int w = img.getWidth() - 1;
        int h = 0;
        while (true) {
            int color = img.getRGB(w, h);
            if (color != scan) {
                break;
            }
            w--;
            h++;
        }
        int count = 0;
        while (true) {
            count++;
            int color = img.getRGB(w, h);
            if (color == scan) {
                break;
            }
            w--;
        }
        w++;
        storeX = w;
        storeY = h;
        storeW = count;
        count = 0;
        while (true) {
            count++;
            int color = img.getRGB(w, h);
            if (color == scan) {
                break;
            }
            h++;
        }
        storeH = count;
    }

    public void enableTransparentWindow(float opacity) {
        frame = new JFrame("    Tomato    ");

//        asscan = new Canvas() {
//            public void paint(Graphics g) {
//                g.drawImage(capture(), 0, 0, null);
//            }
//        };

//        createPanel();
        asscan = new Canvas();
        frame.add(asscan);

//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice gd = ge.getDefaultScreenDevice();

//        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0));
        //If translucent windows aren't supported, exit.

//        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
//            System.err.println(
//                    "Translucency is not supported");
//            System.exit(0);
//        }
        frame.setOpacity(opacity);
        frame.setAlwaysOnTop(true);
        frame.setSize(storeW, storeH);
        frame.setLocation(storeX, storeY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        if (asscan.getBufferStrategy() == null) {
            asscan.createBufferStrategy(2);
        }
        setTransparent(frame);
    }

    public void createHollow() {
//        hollowgram = new Window(null) {
//            @Override
//            public void paint(Graphics g) {
//                g.drawImage(capture(), 1400, 0, null);
//                System.out.println("test");
//            }
//
//            @Override
//            public void update(Graphics g) {
//                paint(g);
//            }
//        };
        hollowgram = new JFrame("t");
        asscan = new Canvas() {
            public void paint(Graphics g) {
                g.drawImage(capture(), 0, 0, null);
            }
        };
//        ((JFrame)hollowgram).setUndecorated(true);
        hollowgram.add(asscan);
//        hollowgram.setOpacity(0.8f);
//        hollowgram.setAlwaysOnTop(true);
//        hollowgram.setBounds(hollowgram.getGraphicsConfiguration().getBounds());
//        hollowgram.setBackground(new Color(0, true));
        hollowgram.setVisible(true);
//        setTransparent(hollowgram);
    }

    public void createPanel() {
        BufferedImage backgroundImage = createBackGroundImage();
        if (backgroundImage != null) {
            width = backgroundImage.getWidth() / 2;
            height = backgroundImage.getHeight() / 2;

            BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.5, 0.5);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            backgroundImage = scaleOp.filter(backgroundImage, after);
        }
//        BufferedImage overlayImage = createOverlayImage();

        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);

        BufferedImage finalBackgroundImage = backgroundImage;
        mainPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (finalBackgroundImage != null) {
                    g.drawImage(finalBackgroundImage, 0, 0, null);
                }
//                BufferedImage cap = capture();
//                Graphics2D g2 = cap.createGraphics();
//                g2.setComposite(alcom);
//                g2.drawImage(cap, null, 0, 0);
//                g2.dispose();

//                g.drawImage(makeImageTranslucent(capture(), 0.5f), 0, 0, null);
                g.drawImage(capture(), 0, 0, null);

//                Graphics2D g2 = (Graphics2D) g;
//                Image newImage = capture().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
//                g2.drawImage(capture(), transform, null);
            }
        };
    }

    private BufferedImage createBackGroundImage() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("assets/map/map6.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        img = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2 = img.createGraphics();
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setStroke(BASIC_STROKE);
//        g2.setColor(Color.blue);
//        int circleCount = 10;
//        for (int i = 0; i < circleCount; i++) {
//            int x = (i * width) / (2 * circleCount);
//            int y = x;
//            int w = height - 2 * x;
//            int h = w;
//            g2.drawOval(x, y, w, h);
//        }
//        g2.dispose();
        return img;
    }

    private BufferedImage createOverlayImage() {
//        BufferedImage img = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2 = img.createGraphics();
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setStroke(BASIC_STROKE);
//        g2.setColor(Color.red);
//        int circleCount = 10;
//        for (int i = 0; i < circleCount + 1; i++) {
//            int x1 = (i * width) / (circleCount);
//            int y1 = 0;
//            int x2 = width - x1;
//            int y2 = height;
//            float alpha = (float) i / circleCount;
//            if (alpha > 1f) {
//                alpha = 1f;
//            }
//            // int rule = AlphaComposite.CLEAR;
//            int rule = AlphaComposite.SRC_OVER;
//            Composite comp = AlphaComposite.getInstance(rule, alpha);
//            g2.setComposite(comp);
//            g2.drawLine(x1, y1, x2, y2);
//        }
//        g2.dispose();
        BufferedImage cap = capture();
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        Graphics2D g = cap.createGraphics();
        g.setComposite(alcom);
        g.drawImage(cap, null, 0, 0);
        g.dispose();
        return cap;
    }

    public static BufferedImage makeImageTranslucent(BufferedImage source, double alpha) {
        BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), java.awt.Transparency.TRANSLUCENT);
        // Get the images graphics
        Graphics2D g = target.createGraphics();
        // Set the Graphics composite to Alpha
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
        // Draw the image into the prepared reciver image
        g.drawImage(source, null, 0, 0);
        // let go of all system resources in this Graphics
        g.dispose();
        // Return the image
        return target;
    }

    public void makeFrame() {
//        transform.translate(150, 140);
//        transform.scale(0.5, 0.5);
        BufferedImage backgroundImage = createBackGroundImage();
        if (backgroundImage != null) {
            width = backgroundImage.getWidth() / 2;
            height = backgroundImage.getHeight() / 2;

            BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.5, 0.5);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            backgroundImage = scaleOp.filter(backgroundImage, after);
        }
//        BufferedImage overlayImage = createOverlayImage();

        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);

//        BufferedImage finalBackgroundImage = backgroundImage;
//        mainPanel = new JPanel() {
//            @Override
//            public void paintComponent(Graphics g) {
//                super.paintComponent(g);
//
//                if (finalBackgroundImage != null) {
//                    g.drawImage(finalBackgroundImage, 0, 0, null);
//                }
////                BufferedImage cap = capture();
////                Graphics2D g2 = cap.createGraphics();
////                g2.setComposite(alcom);
////                g2.drawImage(cap, null, 0, 0);
////                g2.dispose();
//
//                g.drawImage(makeImageTranslucent(capture(), 0.5f), 0, 0, null);
//
////                Graphics2D g2 = (Graphics2D) g;
////                Image newImage = capture().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
////                g2.drawImage(capture(), transform, null);
//            }
//        };

        hollowgram = new Window(null) {
            @Override
            public void paint(Graphics g) {
//                final Font font = getFont().deriveFont(48f);
//                g.setFont(font);
//                g.setColor(Color.RED);
//                final String message = "Hello";
//                FontMetrics metrics = g.getFontMetrics();
//                g.drawString(message,
//                        (getWidth() - metrics.stringWidth(message)) / 2,
//                        (getHeight() - metrics.getHeight()) / 2);
//                g.drawImage(makeImageTranslucent(capture(), 0.5f), -1400, 0, null);
                g.drawImage(capture(), 1400, 0, null);
                System.out.println("test");
            }

            @Override
            public void update(Graphics g) {
                paint(g);
            }
        };
        hollowgram.setOpacity(0.8f);
        hollowgram.setAlwaysOnTop(true);
        hollowgram.setBounds(hollowgram.getGraphicsConfiguration().getBounds());
        hollowgram.setBackground(new Color(0, true));
        hollowgram.setVisible(true);
//        setTransparent(hollowgram);
//        AWTUtilities.setWindowOpacity(hollowgram, 0.80f);


        frame = new JFrame("    Tomato    ");

//        frame.addComponentListener(new ComponentAdapter() {
//            public void componentResized(ComponentEvent componentEvent) {
//                System.out.println("resize " + componentEvent);
//            }
//        });
//
//        frame.addComponentListener(new ComponentAdapter() {
//            public void componentMoved(ComponentEvent componentEvent) {
//                System.out.println("moved " + componentEvent);
//            }
//        });

//        frame.setContentPane(mainPanel);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        System.setProperty("sun.java2d.noddraw", "true");
    }

    public void setLocation(int screen, double x, double y) {
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] d = g.getScreenDevices();

        if (screen >= d.length) {
            screen = d.length - 1;
        }

        Rectangle bounds = d[screen].getDefaultConfiguration().getBounds();

        // Is double?
        if (x == Math.floor(x) && !Double.isInfinite(x)) {
            x *= bounds.x;  // Decimal -> percentage
        }
        if (y == Math.floor(y) && !Double.isInfinite(y)) {
            y *= bounds.y;  // Decimal -> percentage
        }

        x = bounds.x + x;
        y = frame.getY() + y;

        if (x > bounds.x) x = bounds.x;
        if (y > bounds.y) y = bounds.y;

        // If double we do want to floor the value either way
        frame.setLocation((int) x, (int) y);
    }

    private static void setTransparent(Component w) {
        WinDef.HWND hwnd = getHWnd(w);
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    private static WinDef.HWND getHWnd(Component w) {
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(w));
        return hwnd;
    }
}
