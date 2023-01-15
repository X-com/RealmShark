package experimental.input;

import com.sun.jna.*;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.BaseTSD.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinUser.*;

interface LowLevelMouseProc extends HOOKPROC {

    LRESULT callback(int nCode, HWND h, WPARAM wParam, MouseHook.MOUSEHOOKSTRUCT lParam);
}

public final class MouseHook {

    public final User32 USER32INST;
    public final Kernel32 KERNEL32INST;

    public MouseHook() {
        if (!Platform.isWindows()) {
            throw new UnsupportedOperationException("Not supported on this platform.");
        }
        USER32INST = User32.INSTANCE;
        KERNEL32INST = Kernel32.INSTANCE;
        mouseHook = hookTheMouse();
        Native.setProtected(true);

    }
    public static LowLevelMouseProc mouseHook;
    public HHOOK hhk;
    public Thread thrd;
    public boolean threadFinish = true;
    public boolean isHooked = false;
    public static final int WM_MOUSEMOVE = 512;
    public static final int WM_LBUTTONDOWN = 513;
    public static final int WM_LBUTTONUP = 514;
    public static final int WM_RBUTTONDOWN = 516;
    public static final int WM_RBUTTONUP = 517;
    public static final int WM_MBUTTONDOWN = 519;
    public static final int WM_MBUTTONUP = 520;
    public static final int WM_MOUSEWHEEL = 522;

    public void unsetMouseHook() {
        threadFinish = true;
        if (thrd.isAlive()) {
            thrd.interrupt();
            thrd = null;
        }
        isHooked = false;
    }

    public boolean isIsHooked() {
        return isHooked;
    }

    public void setMouseHook() {
        thrd = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isHooked) {
                        hhk = USER32INST.SetWindowsHookEx(User32.WH_MOUSE_LL, mouseHook, KERNEL32INST.GetModuleHandle(null), 0);
                        isHooked = true;
                        MSG msg = new MSG();
                        while ((USER32INST.GetMessage(msg, null, 0, 4)) != 0) {
                            USER32INST.TranslateMessage(msg);
                            USER32INST.DispatchMessage(msg);
                            System.out.print(isHooked);
                            if (!isHooked) {
                                break;
                            }
                        }
                    } else {
                        System.out.println("The Hook is already installed.");
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    System.err.println("Caught exception in MouseHook!");
                }
            }
        });
        threadFinish = false;
        thrd.start();

    }

    public LowLevelMouseProc hookTheMouse() {
        return new LowLevelMouseProc() {
            @Override
            public LRESULT callback(int nCode, HWND h, WPARAM wParam, MOUSEHOOKSTRUCT info) {
                if (nCode >= 0) {
                    switch (wParam.intValue()) {
                        case MouseHook.WM_LBUTTONDOWN: // Left click
                            break;
                        case MouseHook.WM_RBUTTONDOWN: // Right click
                            break;
                        case MouseHook.WM_MBUTTONDOWN:  // Middle click
                            break;
                        case MouseHook.WM_LBUTTONUP:
                            break;
                        case MouseHook.WM_MOUSEMOVE:
                            break;
                        case MouseHook.WM_MOUSEWHEEL: // Scrolling by wheel
                            break;
                        default:
                            break;
                    }
                    if (threadFinish) {
                        USER32INST.PostQuitMessage(0);
                    }
                }
//                Pointer p = info.getPointer();
                return USER32INST.CallNextHookEx(hhk, nCode, wParam, null);
            }
        };
    }

    public class Point extends Structure {

        public class ByReference extends Point implements Structure.ByReference {
        };
        public NativeLong x;
        public NativeLong y;
    }

    @Structure.FieldOrder({"pt", "hwnd", "wHitTestCode", "dwExtraInfo"})
    public static class MOUSEHOOKSTRUCT extends Structure {

        public static class ByReference extends MOUSEHOOKSTRUCT implements Structure.ByReference {
        };
        public POINT pt;
        public HWND hwnd;
        public int wHitTestCode;
        public ULONG_PTR dwExtraInfo;
    }

    public static void main(String[] args) throws InterruptedException {
        MouseHook hooker = new MouseHook();
        hooker.setMouseHook();
//        Thread.sleep(15 * 1000);
//        System.exit(0);
    }
}