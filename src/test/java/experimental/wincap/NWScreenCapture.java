package experimental.wincap;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Native windows screen capture tool for windows.
 * Used to capture buffered images of a specific window.
 * <p>
 * Found at https://stackoverflow.com/questions/464593/how-to-capture-selected-screen-of-other-application-using-java
 * Credits go to wutzebaer.
 */
public class NWScreenCapture {

    // Anything under this line is grabbed from the stackoverflow.
    // Please refer to the link found in the class descriptor
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws AWTException, IOException {
        System.out.println("clearconsole");
        int hWnd = User32.instance.FindWindowA(null, "RotMGExalt");
//        int hWnd = User32.instance.FindWindowA(null, "Settings");
//        int hWnd = User32.instance.FindWindowA(null, "D:\\Programmering\\GitKraken\\RealmShark");
        WindowInfo w = getWindowInfo(hWnd);
//        User32.instance.SetForegroundWindow(w.hwnd);
        double scale = getScaleFactor(hWnd);
        System.out.println(new Rectangle((int) (w.rect.left / scale), (int) (w.rect.top / scale), (int) ((w.rect.right - w.rect.left) / scale), (int) ((w.rect.bottom - w.rect.top) / scale)));

        System.out.println(scale);
//        BufferedImage createScreenCapture = new Robot().createScreenCapture(new Rectangle(w.rect.left, w.rect.top, w.rect.right - w.rect.left, w.rect.bottom - w.rect.top));
//        ImageIO.write(createScreenCapture, "png", new File("screen.png"));
//         listAllWindows();
    }

    public static double getScaleFactor(int hWnd) {
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

    private static void listAllWindows() throws AWTException, IOException {
        final List<WindowInfo> inflList = new ArrayList<WindowInfo>();
        final List<Integer> order = new ArrayList<Integer>();
        int top = User32.instance.GetTopWindow(0);
        while (top != 0) {
            order.add(top);
            top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
        }

        User32.instance.EnumWindows(new WndEnumProc() {
            public boolean callback(int hWnd, int lParam) {
                WindowInfo info = getWindowInfo(hWnd);
                inflList.add(info);
                return true;
            }

        }, 0);
        Collections.sort(inflList, new Comparator<WindowInfo>() {
            public int compare(WindowInfo o1, WindowInfo o2) {
                return order.indexOf(o1.hwnd) - order.indexOf(o2.hwnd);
            }
        });
        for (WindowInfo w : inflList) {
            System.out.println(w);
        }
    }

    public static WindowInfo getWindowInfo(int hWnd) {
        RECT r = new RECT();
        User32.instance.GetWindowRect(hWnd, r);
        byte[] buffer = new byte[1024];
        User32.instance.GetWindowTextA(hWnd, buffer, buffer.length);
        String title = Native.toString(buffer);
        WindowInfo info = new WindowInfo(hWnd, r, title);
        return info;
    }

    /// Natives for Windows ///

    public static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
        boolean callback(int hWnd, int lParam);
    }

    public static interface User32 extends StdCallLibrary {
        public static final String SHELL_TRAY_WND = "Shell_TrayWnd";
        public static final int WM_COMMAND = 0x111;
        public static final int MIN_ALL = 0x1a3;
        public static final int MIN_ALL_UNDO = 0x1a0;

        final User32 instance = (User32) Native.loadLibrary("user32", User32.class);

        WinDef.HDC GetWindowDC(int hWnd);

        boolean EnumWindows(WndEnumProc wndenumproc, int lParam);

        boolean IsWindowVisible(int hWnd);

        int GetWindowRect(int hWnd, RECT r);

        void GetWindowTextA(int hWnd, byte[] buffer, int buflen);

        int GetTopWindow(int hWnd);

        int GetWindow(int hWnd, int flag);

        boolean ShowWindow(int hWnd);

        boolean BringWindowToTop(int hWnd);

        int GetActiveWindow();

        boolean SetForegroundWindow(int hWnd);

        int FindWindowA(String winClass, String title);

        long SendMessageA(int hWnd, int msg, int num1, int num2);

        final int GW_HWNDNEXT = 2;
    }

    public static class RECT extends Structure {
        public int left, top, right, bottom;

        @Override
        protected List<String> getFieldOrder() {
            List<String> order = new ArrayList<>();
            order.add("left");
            order.add("top");
            order.add("right");
            order.add("bottom");
            return order;
        }
    }

    public static class WindowInfo {
        int hwnd;
        RECT rect;
        String title;

        public WindowInfo(int hwnd, RECT rect, String title) {
            this.hwnd = hwnd;
            this.rect = rect;
            this.title = title;
        }

        public String toString() {
            return String.format("(%d,%d)-(%d,%d) : \"%s\"", rect.left, rect.top, rect.right, rect.bottom, title);
        }
    }
}
