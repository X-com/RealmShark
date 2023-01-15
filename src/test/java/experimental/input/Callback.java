package experimental.input;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;

public class Callback {
    public static User32.HHOOK hHook;
    public static User32.LowLevelKeyboardProc lpfn;
    public static volatile boolean quit = false;

    public static void main(String[] args) throws Exception {
//        W32API.HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
//        lpfn = new User32.LowLevelKeyboardProc() {
//            public W32API.LRESULT callback(int nCode, W32API.WPARAM wParam,
//                                           User32.KBDLLHOOKSTRUCT lParam) {
//                System.out.println("here");
//                quit = true;
//                return User32.INSTANCE.CallNextHookEx(hHook, nCode, wParam, lParam
//                        .getPointer());
//            }
//        };
//        hHook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, lpfn, hMod,
//                0);
        if (hHook == null)
            return;
        User32.MSG msg = new User32.MSG();
        while (!quit) {
            User32.INSTANCE.PeekMessage(msg, null, 0, 0, 0);
            Thread.sleep(100);
        }
        if (User32.INSTANCE.UnhookWindowsHookEx(hHook))
            System.out.println("Unhooked");
    }
}