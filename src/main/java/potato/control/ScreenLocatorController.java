package potato.control;

import potato.view.OpenGLPotato;
import potato.model.DataModel;
import util.NativeWindowScreenCapture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenLocatorController {
    private final int FRAME_COLOR = -8553091;
    private final int BOARDER_COLOR = -13224394;
    private final OpenGLPotato renderer;
    Rectangle rect = null;

    public ScreenLocatorController(OpenGLPotato renderer) {
        this.renderer = renderer;
    }

//        A + B = C;
//        A * t + B * (1-t) = C;
//        at + b - bt = c;
//        t(a - b) = c - b;
//        t = (c - b) / (a - b);
//        (c - b) / (a - b) > 0
//        (c > b && a > b) || (c < b && a < b);
//        min(A,c) > b || max(A,c) < b;

    public void calcMapSizeLoc2() {
        rect = NativeWindowScreenCapture.getWindowRect("RotMGExalt", false);
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
        System.out.printf("X:%d Y:%d W:%d H:%d\n", x, y, width, height);
//            model.setSize(width, height);
        renderer.setWindow(x, y, width, height);
        renderer.show();
    }

    private void notFound(int i, String s) {
        renderer.hide();
        System.out.println(i + " " + s);
    }

    public void dispose() {
    }
}
