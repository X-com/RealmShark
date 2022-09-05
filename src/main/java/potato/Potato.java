package potato;

import com.google.gson.*;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;
import packets.Packet;
import packets.PacketType;
import packets.data.GroundTileData;
import packets.data.ObjectData;
import packets.data.WorldPosData;
import packets.incoming.*;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import tomato.Tomato;
import util.NativeWindowScreenCapture;
import util.Pair;
import util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: add tp cooldown
public class Potato extends Thread implements NativeMouseWheelListener, NativeMouseListener, NativeKeyListener {

    static Potato potatoMain;
    private static PacketProcessor packetProcessor;
    static public URL imagePath = Tomato.class.getResource("/icon/potatoIcon.png");
    static private Image icon = Toolkit.getDefaultToolkit().getImage(imagePath);

    int mapWidth = 300;
    int mapHeight = 300;
    int imageScale = 1440;
    int scale = 2048;
    int zoom = 0;
    int imageIndex = 0;
    BufferedImage[] images;
    ArrayList<Heroes>[] mapCoords;
    BufferedImage mapImg;
    int[] zooms = {0, 70, 172, 340, 670, 1560, 15300};
    float[] m = {0, 0.02949208f, 0.0720917f, 0.1447f, 0.286f, 2f / 3f, 6.66f};
    int[] k = {0, 6, 12, 22, 44, 100, 820};
    int[] circleThickness = {5, 7, 8, 9, 10, 16, 130};
    int[] fontSize = {0, 8, 8, 8, 10, 16, 130};
    int thickness = 0;
    int offsetX = 0;
    int offsetY = 0;
    ServerHTTP serverHTTP;
    boolean synchRequests;
    Object LOCK = new Object();
    Render renderer;
    ScreenLocator locator;

    int x;
    int y;
    int playerFileX;
    int playerFileY;

    Robot r;
    Toolkit t = Toolkit.getDefaultToolkit();
    Dimension d = t.getScreenSize();
    Rectangle rectangle = new Rectangle(0, 0, d.width, d.height);
    private long serverTime;
    private long realmClosingTime;
    private int heroesLeft = 0;
    private String castleTimer = "";
    boolean toggleMap = false;
    boolean toggleDots = true;
    boolean showMap = false;
    boolean showHeroes = true;
    boolean showDots = true;
    private boolean startCastleTimer = false;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        new Potato().run();
    }

    public Potato() {
        potatoMain = this;
    }

    public void run() {
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            globalKeyMouseRegister();
            System.out.println("clearconsole");
        } catch (AWTException e) {
            e.printStackTrace();
        }

        Register.INSTANCE.register(PacketType.MAPINFO, this::packets);
        Register.INSTANCE.register(PacketType.UPDATE, this::packets);
        Register.INSTANCE.register(PacketType.NEWTICK, this::packets);
        Register.INSTANCE.register(PacketType.REALM_HERO_LEFT_MSG, this::packets);
        Register.INSTANCE.register(PacketType.TEXT, this::packets);
        if (packetProcessor == null) {
            packetProcessor = new PacketProcessor();
            packetProcessor.start();
        }

        if (serverHTTP == null) {
            try {
                serverHTTP = new ServerHTTP();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }

        images = loadMaps();
        mapImg = images[0];
        mapCoords = loadMapCoords();

        renderer = new Render(1.0f);
        locator = new ScreenLocator(renderer, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                locator.locateLoop();
            }
        }).start();
        calcCoords();
        renderer.renderLoop();
    }

    public void packets(Packet packet) {
        if (packet instanceof MapInfoPacket) {
            MapInfoPacket p = (MapInfoPacket) packet;
            if (p.displayName.equals("{s.rotmg}")) {
                renderer.stuffRender(true);
            } else {
                synchRequests = false;
                startCastleTimer = false;
                heroesLeft = 99;
                renderer.stuffRender(false);
            }
        } else if (packet instanceof UpdatePacket) {
            UpdatePacket p = (UpdatePacket) packet;
            if (p.pos.x != 0 && p.pos.y != 0) {
                x = (int) p.pos.x;
                y = (int) p.pos.y;
                offsetX = (int) (m[zoom] * x + k[zoom]);
                offsetY = (int) (m[zoom] * y + k[zoom]);
//                playerFileX = (int) (((float) x / scale) * 1.4 * storeW) - offsetX;
//                playerFileY = (int) (((float) y / scale) * storeH) - offsetY;
//                System.out.printf("x:%d y:%d\n", x, y);
//                System.out.printf("px:%d py:%d\n", playerFileX, playerFileY);
//                System.out.printf("(y / scale):%f (storeH + zooms[zoom]):%d offsetY:%d\n", ((float) y / scale), (storeH + zooms[zoom]), offsetY);
                calcCoords();
            }
            for (int i = 0; i < p.tiles.length; i++) {
                GroundTileData gtd = p.tiles[i];
                if (gtd.type == 197) {
                    Heroes h = findClosestHero(gtd.x, gtd.y);
                    h.markHero(Heroes.SNAKE_COIL);
                } else if (gtd.type == 12) {
                    Heroes h = findClosestHero(gtd.x, gtd.y);
                    h.markHero(Heroes.HOUSE);
                } else if (gtd.type == 87) {
//                    Heroes h = findClosestHero(gtd.x, gtd.y);
//                    h.markHero(Heroes.MANOR);
                    System.out.println("manor");
                } else if (gtd.type == 241) {
                    Heroes h = findClosestHero(gtd.x, gtd.y);
                    h.markHero(Heroes.MANOR);
                } else if (gtd.type == 297) {
                    Heroes h = findClosestHero(gtd.x, gtd.y);
                    h.markHero(Heroes.PARASITE);
                }
//                if(gtd.type == 70) {
//                    System.out.printf("%d %d\n", gtd.x, gtd.y);
//                }
//                if (gtd.x == x && gtd.y == y) {
//                System.out.println(gtd);
//                }
            }
            for (int i = 0; i < p.newObjects.length; i++) {
                ObjectData od = p.newObjects[i];
                if (od.objectType == 388) {
                    WorldPosData pos = od.status.pos;
                    Heroes h = findClosestHero((int) pos.x, (int) pos.y);
                    h.markHero(Heroes.GRAVE);
                }
//                System.out.println(od);
            }
        } else if (packet instanceof RealmHeroesLeftPacket) {
            RealmHeroesLeftPacket p = (RealmHeroesLeftPacket) packet;
            heroesLeft = p.realmHeroesLeft;
//            if (!startCastleTimer) heroesLeft = String.format("(%d) Heroes %d", imageIndex, p.realmHeroesLeft);
        } else if (packet instanceof NewTickPacket) {
            NewTickPacket p = (NewTickPacket) packet;
            serverTime = p.serverRealTimeMS;
            if (startCastleTimer) {
                long timeSense = serverTime - realmClosingTime;
                int remTime = (int) ((130000 - timeSense) / 1000);
//                System.out.println(timeSense + " " + remTime);
                castleTimer = String.format("Castle %d:%02d", remTime / 60, remTime % 60);
            }
        } else if (packet instanceof TextPacket) {
            TextPacket p = (TextPacket) packet;
//            System.out.println(p.text);
            if (p.text.contains("oryx_closed_realm")) {
//                System.out.println("-----------" + serverTime + "------------");
                realmClosingTime = serverTime;
                startCastleTimer = true;
//                long time = 130114;
            }
        }
    }

    private BufferedImage[] loadMaps() {
        BufferedImage[] img = new BufferedImage[13];
        try {
            for (int i = 1; i <= 13; i++) {
                img[i - 1] = ImageIO.read(new File("assets/map/map" + i + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            img = null;
        }
        return img;
    }

    private ArrayList<Heroes>[] loadMapCoords() {
        ArrayList<Heroes>[] coords = new ArrayList[13];
        try {
            for (int i = 1; i <= 13; i++) {
                File f = new File("assets/map/map" + i + ".txt");
                BufferedReader br = new BufferedReader(new FileReader(f));
                coords[i - 1] = new ArrayList<>();
                String line;
                int index = 0;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] s = line.split(",");
                        int x = Integer.parseInt(s[0]);
                        int y = Integer.parseInt(s[1]);
                        coords[i - 1].add(new Heroes(index, x, y, "new"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
//                        String[] s = line.split(", ");
//                        int x = Integer.parseInt(s[0]);
//                        int y = Integer.parseInt(s[1]);
////                    System.out.printf("x:%d y:%d\n", x, y);
//                        coords[i - 1].add(new Heroes(index, x, y));
                    }
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            coords = null;
        }
        return coords;
    }

    private void calcCoords() {
//        System.out.printf("offset x:%d y:%d\n", offsetX, offsetY);
        int sW = mapWidth + zooms[zoom];
        int sH = mapHeight + zooms[zoom];
        thickness = circleThickness[zoom];
        if (mapCoords == null) return;
        for (Heroes h : mapCoords[imageIndex]) {
            double dx;
            double dy;
            if (h.newCoord) {
                dx = ((float) (h.xx + 150) / 2350);
                dy = ((float) (h.yy + 150) / 2350);
            } else {
                dx = ((float) h.fileX / imageScale) * 1.02;
                dy = (float) h.fileY / imageScale;
            }
            h.drawX = (int) (dx * sW) - offsetX;
            h.drawY = (int) (dy * sH) - offsetY;
        }
    }

    public BufferedImage capture() {
        return r.createScreenCapture(rectangle);
    }

    private void synchMethod() {
        if (synchRequests) return;
        synchRequests = true;
        new Thread(() -> {
            while (synchRequests) {
                if (serverHTTP == null) {
                    try {
                        serverHTTP = new ServerHTTP();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    serverHTTP.synch();
                }
                int wait = 0;
                while (synchRequests && wait < 100) {
                    wait++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }).start();
    }

    private Heroes findClosestHero(int x, int y) {
        float dist = Float.MAX_VALUE;
        Heroes hero = null;
        for (Heroes h : mapCoords[imageIndex]) {
            float dx = h.xx - x;
            float dy = h.yy - y;
            float d = h.squareDistToPlayer();
            if (d < dist) {
                dist = d;
                hero = h;
            }
        }
        return hero;
    }

    public void setSize(int width, int height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    static ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
    static long when = 0;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
//        if (e.getRawCode() == 9) {
//            if ((e.getWhen() - when) < 170) {
//                list.add(new Pair<>(x, y));
//                System.out.println("add----------------");
//                for (Pair p : list) {
//                    System.out.printf("%d,%d\n", p.left(), p.right());
//                }
//                System.out.println("add " + list.size());
//            }
//        } else if (e.getRawCode() == 27) {
//            if ((e.getWhen() - when) < 170) {
//                if (list.size() > 0) list.remove(list.size() - 1);
//                for (Pair p : list) {
//                    System.out.printf("%d,%d\n", p.left(), p.right());
//                }
//                System.out.println("remove " + list.size());
//            }
//        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
//        when = e.getWhen();
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
//        System.out.println("e.getRawCode(): " + e.getRawCode() + " mod: " + e.getModifiers());
        if ((e.getModifiers() % 512) == 3) {
            if (e.getRawCode() == 66) {
                System.out.println("upload");
                if (serverHTTP == null) {
                    try {
                        serverHTTP = new ServerHTTP();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    serverHTTP.uploadMap(imageIndex, mapCoords[imageIndex]);
                    synchMethod();
                }
            } else if (e.getRawCode() == 77) {
                toggleMap = !toggleMap;
                showMap = toggleMap;
                System.out.println("Toggle map: " + toggleMap);
            } else if (e.getRawCode() == 78) {
                toggleDots = !toggleDots;
                showDots = toggleDots;
                System.out.println("Toggle dots: " + toggleDots);
            }
        }
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
//        System.out.println("Click: " + e.getButton() + " mod: " + e.getModifiers() + " " + (e.getModifiers() % 512));
        if ((e.getModifiers() % 512) == 1 && e.getButton() == 1) { // mark active
            Heroes h = findClosestHero(x, y);
            h.setColor(ServerHTTP.MARK_ACTIVE, true);
            if (serverHTTP == null) {
                try {
                    serverHTTP = new ServerHTTP();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            } else {
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, h.index, ServerHTTP.MARK_ACTIVE);
            }
        } else if ((e.getModifiers() % 512) == 1 && e.getButton() == 2) { // mark dead
            Heroes h = findClosestHero(x, y);
            h.setColor(ServerHTTP.MARK_DEAD, true);
            if (serverHTTP == null) {
                try {
                    serverHTTP = new ServerHTTP();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            } else {
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, h.index, ServerHTTP.MARK_DEAD);
            }
        } else if ((e.getModifiers() % 512) == 3 && e.getButton() == 3) {
            System.out.println("synch");
            synchMethod();
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
//        System.out.println("Mosue Wheel Moved: " + e.getWheelRotation() + " modifier: " + e.getModifiers());
        if ((e.getModifiers() % 512) == 0) {
            if (e.getWheelRotation() == 1 && zoom > 0) zoom--;
            else if (e.getWheelRotation() == -1 && zoom < 6) zoom++;
            offsetX = (int) (m[zoom] * x + k[zoom]);
            offsetY = (int) (m[zoom] * y + k[zoom]);
            calcCoords();
        } else if (e.getModifiers() == 3) {
            int ii = imageIndex;
            if (e.getWheelRotation() == 1) {
                if (ii > 0) {
                    ii--;
                } else {
                    ii = 12;
                }
                synchRequests = false;
            } else if (e.getWheelRotation() == -1) {
                if (ii < 12) {
                    ii++;
                } else {
                    ii = 0;
                }
                synchRequests = false;
            }
            if (ii == imageIndex) return;
            for (int i = 0; i < mapCoords[imageIndex].size(); i++) {
                mapCoords[imageIndex].get(i).setColor(0, false);
            }
            imageIndex = ii;
            mapImg = images[imageIndex];
            calcCoords();
            System.out.println("selecting map: " + (imageIndex + 1));
        }
    }

    private void globalKeyMouseRegister() throws AWTException {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            System.exit(1);
        }

        GlobalScreen.addNativeMouseWheelListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeKeyListener(this);
    }

    public class Heroes {
        public static final int HOUSE = 1;
        public static final int GRAVE = 2;
        public static final int SNAKE_COIL = 3;
        public static final int MANOR = 4;
        public static final int PARASITE = 5;
        int index;
        String indexString;
        boolean newCoord = false;
        int xx;
        int yy;
        int scaledX;
        int scaledY;
        int fileX;
        int fileY;
        int drawX;
        int drawY;
        Color color;
        long resetTimer = 0;
        int locationType = 0;

        public Heroes(int index, int x, int y) {
            this.index = index;
            indexString = Integer.toString(index + 1);
            this.fileX = x;
            this.fileY = y;
            this.scaledX = (int) (((float) x / imageScale) * (float) scale);
            this.scaledY = (int) (((float) y / imageScale) * (float) scale);
            this.color = Color.white;
        }

        public Heroes(int index, int x, int y, String aNew) {
            this.index = index;
            indexString = Integer.toString(index + 1);
            this.xx = x;
            this.yy = y;
            this.color = Color.green;
            newCoord = true;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.drawOval(drawX - thickness, drawY - thickness, thickness * 2, thickness * 2);
            setTextCenter(g, indexString, drawX, drawY);
        }

        private void setTextCenter(Graphics2D g, String string, int x, int y) {
            if (fontSize[zoom] == 0) return;
            int stringWidthLength = (int) g.getFontMetrics().getStringBounds(string, g).getWidth();
            int stringHeightLength = (int) g.getFontMetrics().getStringBounds(string, g).getHeight();

            int horizontalCenter = x - stringWidthLength / 2;
            int verticalCenter = y + stringHeightLength / 4;
            g.drawString(string, horizontalCenter, verticalCenter);
        }

        public float squareDistToPlayer() {
            float dx = this.xx - x;
            float dy = this.yy - y;
            return dx * dx + dy * dy;
        }

        public String getColorIndex() {
            if (color == Color.magenta) return "1";
            if (color == Color.darkGray) return "2";
            if (color == Color.white) return "3";
            if (color == Color.green) return "0";
            return "0";
        }

        public void setColor(int colorIndex, boolean setTimer) {
            long t = resetTimer - System.currentTimeMillis();
            if (!setTimer && t > 0) return;
            if (setTimer) resetTimer = System.currentTimeMillis() + 2200;
            switch (colorIndex) {
                case 1:
                    color = Color.magenta; // active
                    return;
                case 2:
                    color = Color.darkGray; // dead
                    return;
                case 3:
                    color = Color.white; // star mark
                    return;
                case 0:
                default:
                    color = Color.green; // not visited
            }
        }

        public String toString() {
            return String.format("Idx:%s x:%d y:%d", indexString, xx, yy);
        }

        public void markHero(int type) {
            if (GRAVE == type && locationType != type) {
                locationType = type;
                setColor(ServerHTTP.MARK_DEAD, true);
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, index, ServerHTTP.MARK_DEAD);
                System.out.println("marked graveyard dead");
            } else if (SNAKE_COIL == type && locationType != type) {
                locationType = type;
                setColor(ServerHTTP.MARK_DEAD, true);
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, index, ServerHTTP.MARK_DEAD);
                System.out.println("snake coil dead");
            } else if (HOUSE == type && locationType != type) {
                locationType = type;
                setColor(ServerHTTP.MARK_DEAD, true);
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, index, ServerHTTP.MARK_DEAD);
                System.out.println("house dead");
            } else if (MANOR == type && locationType != type) {
                locationType = type;
                setColor(ServerHTTP.MARK_DEAD, true);
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, index, ServerHTTP.MARK_DEAD);
                System.out.println("manor dead");
            } else if (PARASITE == type && locationType != type) {
                locationType = type;
                setColor(ServerHTTP.MARK_DEAD, true);
                if (synchRequests) serverHTTP.uploadSingleDot(imageIndex, index, ServerHTTP.MARK_DEAD);
                System.out.println("parasite dead");
            }
        }
    }

    public class ScreenLocator {
        int storeX, storeY, storeW, storeH;
        boolean running;
        Render renderer;
        Potato potato;

        public ScreenLocator(Render renderer, Potato potato) {
            this.renderer = renderer;
            this.potato = potato;
        }

        private void calcMapSizeLoc() {
            int frameColor = -8553091;
            int boarderColor = -13224394;
            Rectangle rect = NativeWindowScreenCapture.getWindowRect("RotMGExalt");
            BufferedImage createScreenCapture = NativeWindowScreenCapture.getWindowImageUndisturbed(rect);
//            System.out.println(createScreenCapture);
            if (createScreenCapture == null) return;
            int w = createScreenCapture.getWidth() - 1;
            int h = 0;

            while (true) {
                int color = 0;
                try {
                    color = createScreenCapture.getRGB(w, h);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.printf("%d %d %s\n", w, h, createScreenCapture);
                    throw new RuntimeException();
                }
                if (color == frameColor) {
                    break;
                }
                h++;
                if (h >= createScreenCapture.getHeight()) {
                    w--;
                    h = 0;
                } else if (w <= 0) {
                    notFound();
                    return;
                }
            }

//            System.out.println("found W:" + w + " H:" + h + " X:" + rect.x + " Y:" + rect.y);
            final int fixedHeight = h;
            int count = 0;
            while (true) {
                int color = createScreenCapture.getRGB(w, h);
                if (color != frameColor) {
                    if (color != boarderColor) {
                        notFound();
                        return;
                    }
                    break;
                }
                count++;
                h++;
                if (h >= createScreenCapture.getHeight()) {
                    notFound();
                    return;
                }
            }
//            System.out.println("count: " + count);

            h = fixedHeight;
            int testH = fixedHeight + 20;
            while (true) {
                int color = createScreenCapture.getRGB(w, h);
                if (color != frameColor) {
                    break;
                }
                h++;
                if (h >= testH) {
                    h = fixedHeight;
                    w--;
                } else if (w <= 0) {
                    notFound();
                    return;
                }
            }
            final int innerTopRightW = w;
            final int innerTopRightH = h;
//            System.out.println("w:" + w + " h:" + h);

            int width = 0;
            while (true) {
                int color = createScreenCapture.getRGB(w, h);
                if (color == frameColor) {
                    break;
                }
                width++;
                w--;
                if (w <= 0) {
                    notFound();
                    return;
                }
            }
            width--;
            int x = rect.x + w + 1;
            int y = rect.y + innerTopRightH;
            w = innerTopRightW;
            int height = 0;
            while (true) {
                int color = createScreenCapture.getRGB(w, h);
                if (color == frameColor) {
                    break;
                }
                height++;
                h++;
            }
            height--;
//            System.out.printf("X:%d Y:%d W:%d H:%d\n", x, y, width, height);
            potato.setSize(width, height);
            renderer.setSizeLoc(x, y, width, height);
            renderer.show();
        }

        private void notFound() {
            renderer.remove();
        }

        public void locateLoop() {
            running = true;
            while (running) {
                calcMapSizeLoc();
                int counter = 0;
                while (running && counter < 20) {
                    counter++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class ServerHTTP {
        URL url;
        public static final int MARK_ACTIVE = 1;
        public static final int MARK_DEAD = 2;

        public ServerHTTP() throws MalformedURLException {
            url = new URL("http://ec2-3-90-180-208.compute-1.amazonaws.com:8080/rum");
        }

        public void uploadSingleDot(int mapIndex, int markIndex, int colorIndex) {
            HttpURLConnection http = null;
            try {
                http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Content-Type", "application/json");
                http.setDoOutput(true);
                http.setRequestMethod("PUT");

                String jsonInputString = "{\"mapIdx\":\"" + mapIndex + "\",\"server\":\"...\",\"" + markIndex + "\":\"" + colorIndex + "\"}";

                try (OutputStream os = http.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                int response = http.getResponseCode();
//                System.out.println(response + " " + http.getResponseMessage());
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }

        public void uploadMap(int mapIndex, ArrayList<Heroes> list) {
            HttpURLConnection http = null;
            try {
                http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Content-Type", "application/json");
                http.setDoOutput(true);
                http.setRequestMethod("POST");

                String str = "{\"0\": \"0\", \"1\": \"0\", \"2\": \"0\", \"3\": \"0\", \"4\": \"0\", \"5\": \"0\", \"6\": \"0\", \"7\": \"0\", \"8\": \"0\", \"9\": \"0\", \"10\": \"0\", \"11\": \"0\", \"12\": \"0\", \"13\": \"0\", \"14\": \"0\", \"15\": \"0\", \"16\": \"0\", \"17\": \"0\", \"18\": \"0\", \"19\": \"0\", \"20\": \"0\", \"21\": \"0\", \"22\": \"0\", \"23\": \"0\", \"24\": \"0\", \"25\": \"0\", \"26\": \"0\", \"27\": \"0\", \"28\": \"0\", \"29\": \"0\", \"30\": \"0\", \"31\": \"0\", \"32\": \"0\", \"33\": \"0\", \"34\": \"0\", \"35\": \"0\", \"36\": \"0\", \"37\": \"0\", \"38\": \"0\", \"39\": \"0\", \"40\": \"0\", \"41\": \"0\", \"42\": \"0\", \"43\": \"0\", \"44\": \"0\", \"45\": \"0\", \"46\": \"0\", \"47\": \"0\", \"48\": \"0\", \"49\": \"0\", \"50\": \"0\", \"51\": \"0\", \"52\": \"0\", \"53\": \"0\", \"54\": \"0\", \"55\": \"0\", \"56\": \"0\", \"57\": \"0\", \"58\": \"0\", \"59\": \"0\", \"60\": \"0\", \"61\": \"0\", \"62\": \"0\", \"63\": \"0\", \"64\": \"0\", \"65\": \"0\", \"66\": \"0\", \"67\": \"0\", \"68\": \"0\", \"69\": \"0\", \"70\": \"0\", \"71\": \"0\", \"72\": \"0\", \"73\": \"0\", \"74\": \"0\", \"75\": \"0\", \"76\": \"0\", \"77\": \"0\", \"78\": \"0\", \"79\": \"0\", \"80\": \"0\", \"81\": \"0\", \"82\": \"0\", \"83\": \"0\", \"84\": \"0\", \"85\": \"0\", \"86\": \"0\", \"87\": \"0\", \"88\": \"0\", \"89\": \"0\", \"90\": \"0\", \"91\": \"0\", \"92\": \"0\", \"93\": \"0\", \"94\": \"0\", \"95\": \"0\", \"96\": \"0\", \"97\": \"0\", \"98\": \"0\", \"99\": \"0\", \"100\": \"0\", \"101\": \"0\", \"102\": \"0\", \"103\": \"0\", \"104\": \"0\", \"105\": \"0\", \"106\": \"0\", \"107\": \"0\", \"108\": \"0\", \"109\": \"0\", \"110\": \"0\", \"111\": \"0\", \"112\": \"0\", \"113\": \"0\", \"114\": \"0\", \"115\": \"0\", \"116\": \"0\", \"117\": \"0\", \"118\": \"0\", \"119\": \"0\", \"120\": \"0\", \"121\": \"0\", \"122\": \"0\", \"123\": \"0\", \"124\": \"0\", \"125\": \"0\", \"126\": \"0\", \"127\": \"0\", \"mapIdx\": \"0\", \"server\": \"...\"}";
                JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
                jsonObject.addProperty("mapIdx", Integer.toString(mapIndex));
                for (int i = 0; i < 128; i++) {
                    String num = "0";
                    if (i < list.size()) num = list.get(i).getColorIndex();
                    jsonObject.addProperty(Integer.toString(i), num);
                }

                try (OutputStream os = http.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }

        public void synch() {
            HttpURLConnection http = null;
            try {
                http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Accept", "application/json");

                String jsonString = "";
                try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    jsonString = response.toString();
                }
                JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                int mapIndex = jsonObject.get("mapIdx").getAsJsonPrimitive().getAsInt();
                imageIndex = mapIndex;
                mapImg = images[imageIndex];
                JsonArray ja = jsonObject.get("markerPiIdxs").getAsJsonArray();
                int[] markers = new int[ja.size()];
                for (int i = 0; i < mapCoords[imageIndex].size(); i++) {
                    mapCoords[imageIndex].get(i).setColor(ja.get(i).getAsJsonPrimitive().getAsInt(), false);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }
    }

    public class Render {
        int width = 300;
        int height = 300;
        JFrame menuFrame;
        JWindow frame;
        Canvas asscan;
        JPanel panel;
        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);

        public boolean running;

        public Render(float opacity) {
            smallWindow();

            frame = new JWindow();
            frame.setIconImage(icon);
            frame.setAlwaysOnTop(true);
            frame.setBackground(new Color(0, 0, 0, 0));
//            asscan = new Canvas();
            panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    render((Graphics2D) g);
                }
            };
            panel.setOpaque(false);
//            panel.add(asscan);
//            panel.setOpaque(false);
            frame.add(panel);
//            frame.add(asscan);

            frame.setSize(1, 1);
            frame.setLocation(-1, -1);
//
//            panel.setSize(storeW, storeH);
//            asscan.setSize(storeW, storeH);

            frame.setVisible(true);
            setTransparent(frame);
//            if (asscan.getBufferStrategy() == null) {
//                asscan.createBufferStrategy(3);
//            }
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
                g.drawImage(mapImg, -offsetX, -offsetY, width + zooms[zoom], height + zooms[zoom], null);
                g.setComposite(originalComposite);
            }
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Monospaced", Font.PLAIN, fontSize[zoom]));
            if (showDots && !startCastleTimer) {
                for (Heroes h : mapCoords[imageIndex]) {
                    h.draw(g);
                }
            }
            g.setColor(Color.lightGray);
            if (startCastleTimer) {
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(castleTimer, 5, 20);
            } else if (showHeroes) {
                g.setFont(new Font("Arial Black", Font.PLAIN, 20));
                g.drawString(String.format("(%d) Heroes:%d", imageIndex + 1, heroesLeft), 5, 20);
            }
            g.setFont(new Font("Arial Black", Font.PLAIN, 10));
            g.drawString(String.format("x:%d y:%d", x, y), 5, height - 5);
            g.dispose();
        }

        public void stuffRender(boolean b) {
            showDots = b && toggleDots;
            showMap = b && toggleMap;
            showHeroes = b;
        }

        public void renderLoop() {
            running = true;
            while (running) {
                try {
                    panel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
    }
}
