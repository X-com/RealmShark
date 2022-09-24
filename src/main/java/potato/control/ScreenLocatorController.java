package potato.control;

import potato.model.DataModel;
import potato.view.RenderViewer;
import util.NativeWindowScreenCapture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenLocatorController {
    private final int FRAME_COLOR = -8553091;
    private final int BOARDER_COLOR = -13224394;
    private boolean running;
    private final RenderViewer renderer;
    private final DataModel model;
    private int storeX;
    private int storeY;
    private int storeW;
    private int storeH;
    Rectangle rect = null;

    public ScreenLocatorController(RenderViewer renderer, DataModel model) {
        this.renderer = renderer;
        this.model = model;
    }

//    private void calcMapSizeLoc() {
//        Rectangle rect = NativeWindowScreenCapture.getWindowRect("RotMGExalt", false);
//        BufferedImage createScreenCapture = NativeWindowScreenCapture.getWindowImageUndisturbed(rect);
////            System.out.println(createScreenCapture);
//        if (createScreenCapture == null) return;
//        int w = createScreenCapture.getWidth() - 1;
//        int h = 0;
//
//        while (true) {
//            int color;
//            try {
//                color = createScreenCapture.getRGB(w, h);
//            } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.printf("%d %d %s\n", w, h, createScreenCapture);
//                throw new RuntimeException();
//            }
//            if (color == FRAME_COLOR) {
//                break;
//            }
//            h++;
//            if (h >= createScreenCapture.getHeight()) {
//                w--;
//                h = 0;
//            } else if (w <= 0) {
//                String s = String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y);
//                notFound(1, s);
//                return;
//            }
//        }
//
//        final int fixedHeight = h;
//        while (true) {
//            int color = createScreenCapture.getRGB(w, h);
//            if (color != FRAME_COLOR) {
//                if (color != BOARDER_COLOR && createScreenCapture.getRGB(w, h + 1) != BOARDER_COLOR) {
//                    String s = String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y);
//                    notFound(2, s);
//                    return;
//                }
//                break;
//            }
//            h++;
//            if (h >= createScreenCapture.getHeight()) {
//                String s = String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y);
//                notFound(3, s);
//                return;
//            }
//        }
//
//        h = fixedHeight;
//        int testH = fixedHeight + 20;
//        while (true) {
//            int color = createScreenCapture.getRGB(w, h);
//            if (color != FRAME_COLOR) {
//                break;
//            }
//            h++;
//            if (h >= testH) {
//                h = fixedHeight;
//                w--;
//            } else if (w <= 0) {
//                String s = String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y);
//                notFound(4, s);
//                return;
//            }
//        }
//        final int innerTopRightW = w;
//        final int innerTopRightH = h;
////            System.out.println("w:" + w + " h:" + h);
//
//        int width = 0;
//        while (true) {
//            int color = createScreenCapture.getRGB(w, h);
//            if (color == FRAME_COLOR) {
//                break;
//            }
//            width++;
//            w--;
//            if (w <= 0) {
//                notFound(5, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
//                return;
//            }
//        }
//        width--;
//        int x = rect.x + w + 1;
//        int y = rect.y + innerTopRightH;
//        w = innerTopRightW;
//        int height = 0;
//        while (true) {
//            int color = createScreenCapture.getRGB(w, h);
//            if (color == FRAME_COLOR) {
//                break;
//            }
//            height++;
//            h++;
//        }
//        height--;
//        if (x != storeX || y != storeY || width != storeW || height != storeH) {
//            System.out.printf("X:%d Y:%d W:%d H:%d\n", x, y, width, height);
//            model.setSize(width, height);
//            renderer.setSizeLoc(x, y, width, height);
//            renderer.show();
//            storeX = x;
//            storeY = y;
//            storeW = width;
//            storeH = height;
//        }
//    }

    private void calcMapSizeLoc2() {
        Rectangle newRect = NativeWindowScreenCapture.getWindowRect("RotMGExalt", false);
        if(newRect.equals(rect)) return;
        rect = newRect;
        BufferedImage createScreenCapture = NativeWindowScreenCapture.getWindowImageUndisturbed(rect);
        if (createScreenCapture == null) return;
        int w = createScreenCapture.getWidth() - 1;
        int h = 0;

        while (true) {
            int color;
            try {
                color = createScreenCapture.getRGB(w, h);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.printf("%d %d %s\n", w, h, createScreenCapture);
                throw new RuntimeException();
            }
            if (color == FRAME_COLOR) {
                break;
            }
            h++;
            if (h >= createScreenCapture.getHeight()) {
                w--;
                h = 0;
            } else if (w <= 0) {
                notFound(1, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
                return;
            }
        }
        int tempW = w;
        int tempH = h;
        int y = h;
        int height = 0;
        while (true) {
            int color = createScreenCapture.getRGB(w, h);
            if (color != FRAME_COLOR) {
                if (color != BOARDER_COLOR && createScreenCapture.getRGB(w, h + 2) != BOARDER_COLOR) {
                    notFound(2, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
                    return;
                }
                break;
            }
            h++;
            height++;
            if (h >= createScreenCapture.getHeight()) {
                notFound(3, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
                return;
            }
        }
        h += 3;
        int width = 0;
        while (true) {
            int color = createScreenCapture.getRGB(w, h);
            if (color != BOARDER_COLOR) {
                break;
            }
            w--;
            width++;
            if (w <= 0) {
                notFound(4, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
                return;
            }
        }
        int x = w + 1;
        int boarder = 0;
        w = tempW;
        h = tempH + height / 2;
        while (true) {
            int color = createScreenCapture.getRGB(w, h);
            if (color != FRAME_COLOR) {
                break;
            }
            w--;
            boarder++;
            if (h >= createScreenCapture.getHeight()) {
                notFound(5, String.format("failed W:%d H:%d X:%d Y:%d\n", w, h, rect.x, rect.y));
                return;
            }
        }

        x = x + rect.x + boarder;
        y = y + rect.y + boarder;
        width -= boarder * 2;
        height -= boarder * 2 - 1;
        if (x != storeX || y != storeY || width != storeW || height != storeH) {
            System.out.printf("X:%d Y:%d W:%d H:%d\n", x, y, width, height);
            model.setSize(width, height);
            renderer.setSizeLoc(x, y, width, height);
            renderer.show();
            storeX = x;
            storeY = y;
            storeW = width;
            storeH = height;
        }
    }

    private void notFound(int i, String s) {
        renderer.remove();
        storeX = 0;
        storeY = 0;
        storeW = 0;
        storeH = 0;
        System.out.println(i + " " + s);
    }

    public void locateLoop() {
        new Thread(() -> {
            running = true;
            while (running) {
                calcMapSizeLoc2();
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
        }).start();
    }

    public void dispose() {
        running = false;
    }
}
