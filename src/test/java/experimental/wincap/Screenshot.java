package experimental.wincap;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Screenshot {
    public static void main(String[] args) throws AWTException, IOException {
        test();
//        if(true) return;
        List<WindowInfo> inflList = new ArrayList<>();
        WinDef.RECT rect = new WinDef.RECT();
//        User32.INSTANCE.GetWindowAttribute(hw, rect);
        System.out.println(rect);
        User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
            int count;

            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer data) {
                WindowInfo info = getWindowInfo(hWnd);
                inflList.add(info);
                return true;
            }
        }, null);

        int a = 0;
        System.out.println(inflList.size());
        for (WindowInfo w : inflList) {
            if (
                    w.title.contains("ReadPlease Plus 2003:") ||
                w.title.contains("RotMGExalt")
            ) {
                    System.out.println(w);
//                    BufferedImage createScreenCapture = new Robot().createScreenCapture(new Rectangle(w.rect.left + a, w.rect.top + a, w.rect.right - w.rect.left - a*2, w.rect.bottom - w.rect.top - a*2));
//                    ImageIO.write(createScreenCapture, "png", new File(w.title.substring(0, 10) + ".png"));
            }
        }

//        int hWnd = User32.instance.FindWindowA(null, "RotMGExalt");
//        RECT r = new RECT();
//        User32.instance.GetWindowRect(hWnd, r);
//        System.out.println(r);

//        int hWnd = User32.instance.FindWindowA(null, "Minesweeper X");
//        WindowInfo w = getWindowInfo(hWnd);
//        User32.instance.SetForegroundWindow(w.hwnd);
//        ImageIO.write(createScreenCapture, "png", new File("screen.png"));

        // listAllWindows();
    }

    public static void test() {
        WinDef.HWND hw = User32.INSTANCE.FindWindow(null, "ReadPlease Plus 2003: .\\Help\\rpInstructions.txt");

//        User32.INSTANCE.SetWindowPos(hw, null, -7, 0, 1920+14, 1080+7, 0);

        WinNT.HRESULT hresult = DwmAPI.instance.DwmExtendFrameIntoClientArea(hw, 0);

        System.out.println(hresult);
    }

//    private static void listAllWindows() throws AWTException, IOException {
//        final List<WindowInfo> inflList = new ArrayList<WindowInfo>();
//        final List<Integer> order = new ArrayList<Integer>();
//        int top = User32.instance.GetTopWindow(0);
//        while (top != 0) {
//            order.add(top);
//            top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
//        }
//
//        User32.instance.EnumWindows(new WndEnumProc() {
//            public boolean callback(int hWnd, int lParam) {
//                WindowInfo info = getWindowInfo(hWnd);
//                inflList.add(info);
//                return true;
//            }
//
//        }, 0);
//        Collections.sort(inflList, new Comparator<WindowInfo>() {
//            public int compare(WindowInfo o1, WindowInfo o2) {
//                return order.indexOf(o1.hwnd) - order.indexOf(o2.hwnd);
//            }
//        });
//        for (WindowInfo w : inflList) {
//            System.out.println(w);
//        }
//    }

    public static WindowInfo getWindowInfo(WinDef.HWND hWnd) {
        WinDef.RECT r = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hWnd, r);
        char[] buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, buffer, buffer.length);
        String title = Native.toString(buffer);
        WindowInfo info = new WindowInfo(hWnd, r, title);
        return info;
    }

//    public static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
//        boolean callback(int hWnd, int lParam);
//    }

//    public static interface User32 extends StdCallLibrary {
//        public static final String SHELL_TRAY_WND = "Shell_TrayWnd";
//        public static final int WM_COMMAND = 0x111;
//        public static final int MIN_ALL = 0x1a3;
//        public static final int MIN_ALL_UNDO = 0x1a0;
//
//        final User32 instance = (User32) Native.loadLibrary("user32", User32.class);
//
//        boolean EnumWindows(WndEnumProc wndenumproc, int lParam);
//
//        boolean IsWindowVisible(int hWnd);
//
//        int GetWindowRect(int hWnd, RECT r);
//
//        void GetWindowTextA(int hWnd, byte[] buffer, int buflen);
//
//        int GetTopWindow(int hWnd);
//
//        int GetWindow(int hWnd, int flag);
//
//        boolean ShowWindow(int hWnd);
//
//        boolean BringWindowToTop(int hWnd);
//
//        int GetActiveWindow();
//
//        boolean SetForegroundWindow(int hWnd);
//
//        int FindWindowA(String winClass, String title);
//
//        long SendMessageA(int hWnd, int msg, int num1, int num2);
//
//        final int GW_HWNDNEXT = 2;
//    }

    public static interface DwmAPI extends StdCallLibrary {

        final DwmAPI instance = (DwmAPI) Native.loadLibrary("awt_Toolkit", DwmAPI.class);

        WinNT.HRESULT DwmExtendFrameIntoClientArea(WinDef.HWND hWnd, int margins);
    }

//    public static class RECT extends Structure {
//        public int left, top, right, bottom;
//
//        @Override
//        protected List<String> getFieldOrder() {
//            java.util.List<String> order = new ArrayList<>(super.getFieldOrder());
//            order.add("left");
//            order.add("top");
//            order.add("right");
//            order.add("bottom");
//            return order;
//        }
//    }

    public static class WindowInfo {
        WinDef.HWND hwnd;
        WinDef.RECT rect;
        String title;

        public WindowInfo(WinDef.HWND hwnd, WinDef.RECT rect, String title) {
            this.hwnd = hwnd;
            this.rect = rect;
            this.title = title;
        }

        public String toString() {
            return String.format("(%d,%d)-(%d,%d) : \"%s\"", rect.left, rect.top, rect.right, rect.bottom, title);
        }
    }
}