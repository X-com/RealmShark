package example.damagecalc;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;

public class Hook {

    static Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);
    static User32 user32 = Native.load("user32", User32.class);

    public static void main(String[] args) {
        int pid = getProcessId("RotMGExalt"); // get our process ID
        if (pid == -1) return;
        HANDLE readprocess = openProcess(0x0010, pid); // open the process ID with read priviledges.

        int size = 4; // we want to read 4 bytes
        Memory read = readMemory(readprocess, 0x2158DD8C540L, size); // read 4 bytes of memory starting at the address 0x00AB0C62.

        System.out.println(read.getInt(0)); // print out the value!
    }

    public static int getProcessId(String window) {
        ArrayList<WinDef.HWND> list = getWinHwnd(window);

        if (list.size() != 1) return -1;

        IntByReference pid = new IntByReference(0);
        user32.GetWindowThreadProcessId(list.get(0), pid);

        return pid.getValue();
    }

    public static HANDLE openProcess(int permissions, int pid) {
        HANDLE process = kernel32.OpenProcess(permissions, true, pid);
        return process;
    }

    private static ArrayList<WinDef.HWND> getWinHwnd(final String startOfWindowName) {
        final ArrayList<WinDef.HWND> hWndC = new ArrayList<>();
        User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer userData) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
                String wText = Native.toString(windowText).trim();

                if (!wText.isEmpty() && wText.startsWith(startOfWindowName)) {
                    hWndC.add(hWnd);
                }
                return true;
            }
        }, null);
        return hWndC;
    }

    public static Memory readMemory(HANDLE process, long addre, int bytesToRead) {
        IntByReference read = new IntByReference(0);
        Memory output = new Memory(bytesToRead);
        Pointer address = new Pointer(addre);

        kernel32.ReadProcessMemory(process, address, output, bytesToRead, read);
        return output;
    }
}
