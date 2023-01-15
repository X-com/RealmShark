package experimental.input;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;

import java.util.Arrays;

public class MainTestKeyHook {

    public static void main(String[] args) throws Exception {
        HOOKPROC hookProc = new HOOKPROC_bg();
        HINSTANCE hInst = Kernel32.INSTANCE.GetModuleHandle(null);

        User32.HHOOK hHook1 = User32.INSTANCE.SetWindowsHookEx(User32.WH_MOUSE_LL, hookProc, hInst, 0);
//        User32.HHOOK hHook2 = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, hookProc, hInst, 0);

        if (hHook1 == null)
            return;
//        if (hHook2 == null)
//            return;
        User32.MSG msg = new User32.MSG();
        System.err.println("Please press any key ....");
        while (true) {
            User32.INSTANCE.GetMessage(msg, null, 0, 0);
        }
    }

    @Structure.FieldOrder({"pt", "hwnd", "wHitTestCode", "dwExtraInfo"})
    public static class MOUSEHOOKSTRUCT extends Structure {

        public static class ByReference extends MOUSEHOOKSTRUCT implements Structure.ByReference {
        };
        public WinDef.POINT pt;
        public WinDef.HWND hwnd;
        public int wHitTestCode;
        public BaseTSD.ULONG_PTR dwExtraInfo;

    }
}

class HOOKPROC_bg implements HOOKPROC {

    public HOOKPROC_bg() {
    }

    public LRESULT callback(int nCode, WPARAM wParam, Pointer lParam) {
//        Pointer p = new Pointer(lParam.longValue());
//        MainTestKeyHook.MOUSEHOOKSTRUCT s = new MainTestKeyHook.MOUSEHOOKSTRUCT(lParam);
        System.err.println("callback nCode: " + nCode + " " + wParam.intValue() + " " + lParam.getInt(0) + " " + lParam.getInt(4) + " " + lParam.getInt(8) + " " + lParam.getInt(12) + " " + lParam.getInt(16) + " " + Arrays.toString(lParam.getByteArray(0, 20)));
        return null;
    }
}