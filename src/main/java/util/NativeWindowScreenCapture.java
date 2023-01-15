package util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Native windows screen capture tool for windows.
 * Used to capture buffered images of a specific window.
 * <p>
 * Found at https://stackoverflow.com/questions/464593/how-to-capture-selected-screen-of-other-application-using-java
 * Credits go to wutzebaer.
 */
public class NativeWindowScreenCapture {

    /**
     * This method only works if the window is not minimized.
     * The image will also have black pixels if the window is
     * located outside the bounds of main monitor.
     * <p>
     * WARNING! This will force the window being captured to the top.
     *
     * @param windowName Window name that needs to be captured.
     * @return Image of the window as a BufferedImage.
     */
    static public BufferedImage getWindowImage(String windowName) {
        HWND hWnd = User32.INSTANCE.FindWindow(null, windowName);
        WindowInfo w = getWindowInfo(hWnd);
        User32.INSTANCE.SetForegroundWindow(w.hwnd);
        try {
            return new Robot().createScreenCapture(new Rectangle(w.rect.left, w.rect.top, w.rect.right - w.rect.left, w.rect.bottom - w.rect.top));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method only works if the window is not minimized.
     * The image will also have black pixels if the window is
     * located outside the bounds of main monitor.
     *
     * @param windowName Window name that needs to be captured.
     * @return Image of the window as a BufferedImage.
     */
    static public BufferedImage getWindowImageUndisturbed(String windowName) {
        HWND hWnd = User32.INSTANCE.FindWindow(null, windowName);
        WindowInfo w = getWindowInfo(hWnd);
        try {
            return new Robot().createScreenCapture(new Rectangle(w.rect.left, w.rect.top, w.rect.right - w.rect.left, w.rect.bottom - w.rect.top));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method only works if the window is not minimized.
     * The image will also have black pixels if the window is
     * located outside the bounds of main monitor.
     *
     * @param rect Rectangle representing the location of the window on the screen.
     * @return Image of the window as a BufferedImage.
     */
    static public BufferedImage getWindowImageUndisturbed(Rectangle rect) {
        if (rect.width == 0 || rect.height == 0) return null;
        try {
            return new Robot().createScreenCapture(rect);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a rectangle indicating where the image is on the screen as.
     *
     * @param windowName Window name.
     * @param scaling    Enables windows scaling option for users using scaling on Windows.
     * @return Rectangle of where it is on screen (left, top, width, height)
     */
    public static Rectangle getWindowRect(String windowName, boolean scaling) {
        HWND hWnd = User32.INSTANCE.FindWindow(null, windowName);
        WindowInfo w = getWindowInfo(hWnd);
        double scale;
        if (scaling) {
            scale = getScaleFactor(hWnd);
        } else {
            scale = 1;
        }
        return new Rectangle((int) (w.rect.left / scale), (int) (w.rect.top / scale), (int) ((w.rect.right - w.rect.left) / scale), (int) ((w.rect.bottom - w.rect.top) / scale));
    }

    private static double getScaleFactor(HWND hWnd) {
        WinDef.HDC hdc = GDI32.INSTANCE.CreateCompatibleDC(null);
        if (hdc != null) {
            int actual = GDI32.INSTANCE.GetDeviceCaps(hdc, 10 /* VERTRES */);
            int logical = GDI32.INSTANCE.GetDeviceCaps(hdc, 117 /* DESKTOPVERTRES */);
            GDI32.INSTANCE.DeleteDC(hdc);
            // JDK11 seems to always return 1, use fallback below
            if (logical != 0 && logical / actual > 1) {
                return (double) logical / actual;
            }
        }
        return Toolkit.getDefaultToolkit().getScreenResolution() / 96.0d;
    }

    private static WindowInfo getWindowInfo(HWND hWnd) {
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, r);
        char[] buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, buffer, buffer.length);
        String title = Native.toString(buffer);
        WindowInfo info = new WindowInfo(hWnd, r, title);
        return info;
    }

    private static class WindowInfo {
        HWND hwnd;
        RECT rect;
        String title;

        public WindowInfo(HWND hwnd, RECT rect, String title) {
            this.hwnd = hwnd;
            this.rect = rect;
            this.title = title;
        }

        public String toString() {
            return String.format("(%d,%d)-(%d,%d) : \"%s\"", rect.left, rect.top, rect.right, rect.bottom, title);
        }
    }
}
