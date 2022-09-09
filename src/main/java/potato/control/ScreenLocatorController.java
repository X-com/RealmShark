package potato.control;

import potato.model.DataModel;
import potato.view.RenderViewer;
import util.NativeWindowScreenCapture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenLocatorController {
    boolean running;
    RenderViewer renderer; // TODO remove
    DataModel model;

    public ScreenLocatorController(RenderViewer renderer, DataModel model) {
        this.renderer = renderer;
        this.model = model;
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
        while (true) {
            int color = createScreenCapture.getRGB(w, h);
            if (color != frameColor) {
                if (color != boarderColor) {
                    notFound();
                    return;
                }
                break;
            }
            h++;
            if (h >= createScreenCapture.getHeight()) {
                notFound();
                return;
            }
        }

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
        model.setSize(width, height);
        renderer.setSizeLoc(x, y, width, height);
        renderer.show();
    }

    private void notFound() {
        renderer.remove();
    }

    public void locateLoop() {
        new Thread(() -> {
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
        }).start();
    }
}
