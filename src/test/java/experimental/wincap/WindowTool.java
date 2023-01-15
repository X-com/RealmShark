package experimental.wincap;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WindowTool {

    public static void main(String[] args) {
        WindowTool m = new WindowTool();
        final List<WindowInfo> inflList = new ArrayList<WindowInfo>();
        final List<Integer> order = new ArrayList<Integer>();
        int top = User32.instance.GetTopWindow(0);
        while (top != 0) {
            order.add(top);
            top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
        }

        User32.instance.EnumWindows(new WndEnumProc() {
            public boolean callback(int hWnd, int lParam) {
                if (User32.instance.IsWindowVisible(hWnd)) {
                    RECT r = new RECT();
                    User32.instance.GetWindowRect(hWnd, r);
                    if (r.left > -32000) {     // If it's not minimized
                        byte[] buffer = new byte[1024];
                        User32.instance.GetWindowTextA(hWnd, buffer, buffer.length);
                        String title = Native.toString(buffer);
                        inflList.add(new WindowInfo(hWnd, r, title));
                    }
                }
                return true;
            }
        }, 0);

        Collections.sort(inflList, new Comparator<WindowInfo>() {
            public int compare(WindowInfo o1, WindowInfo o2) {
                return order.indexOf(o1.hwnd)-order.indexOf(o2.hwnd);
            }
        });
        for (WindowInfo w : inflList) {
            System.out.println(w);
        }
    }

    public static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
        boolean callback(int hWnd, int lParam);
    }

    public static interface User32 extends StdCallLibrary {
        final User32 instance = (User32) Native.loadLibrary ("user32", User32.class);
        final int GW_HWNDNEXT = 2;

        boolean EnumWindows(WndEnumProc wndenumproc, int lParam);
        boolean IsWindowVisible(int hWnd);
        int GetWindowRect(int hWnd, RECT r);
        void GetWindowTextA(int hWnd, byte[] buffer, int buflen);
        int GetTopWindow(int hWnd);
        int GetWindow(int hWnd, int flag);
    }

    public static class RECT extends Structure {
        public int left, top, right, bottom;
    }

    public static class WindowInfo {
        public final int hwnd;
        public final RECT rect;
        public final String title;
        public WindowInfo(int hwnd, RECT rect, String title) {
            this.hwnd = hwnd;
            this.rect = rect;
            this.title = title;
        }

        public String toString() {
            return String.format("(%d,%d)-(%d,%d) : \"%s\"",
                    rect.left, rect.top,
                    rect.right, rect.bottom,
                    title);
        }
    }
}