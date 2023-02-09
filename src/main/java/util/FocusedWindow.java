package util;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public class FocusedWindow {

    final static int PROCESS_VM_READ = 0x0010;
    final static int PROCESS_QUERY_INFORMATION = 0x0400;
    final static User32 user32 = User32.INSTANCE;
    final static Kernel32 kernel32 = Kernel32.INSTANCE;

    public interface Psapi extends StdCallLibrary {
        Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class);

        WinDef.DWORD GetModuleBaseNameW(Pointer hProcess, Pointer hModule, byte[] lpBaseName, int nSize);
    }

    public static String getWindowFocus() {
        if (Platform.isWindows()) {
            WinDef.HWND windowHandle = user32.GetForegroundWindow();
            IntByReference pid = new IntByReference();
            user32.GetWindowThreadProcessId(windowHandle, pid);
            WinNT.HANDLE processHandle = kernel32.OpenProcess(PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, true, pid.getValue());
            if (processHandle == null) return "";

            byte[] filename = new byte[512];
            Psapi.INSTANCE.GetModuleBaseNameW(processHandle.getPointer(), Pointer.NULL, filename, filename.length);
            StringBuilder sb = new StringBuilder();
            for (byte b : filename) {
                if (b != 0) sb.append(Character.toChars(b));
            }
            return sb.toString();
        }
        return "";
    }
}
