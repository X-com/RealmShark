package pipe;

import assembly.GHMemory;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.io.File;
import java.util.ArrayList;

public class Injector {

    static Kernel32 kernel32 = Native.load("kernel32.dll", Kernel32.class, W32APIOptions.ASCII_OPTIONS);
    static PsapiExt psapi = Native.load("psapi", PsapiExt.class, W32APIOptions.UNICODE_OPTIONS);

    public static void main(String[] args) {
        injectDLL();
    }

    public static void injectDLL() {
        ArrayList<HWND> list = GHMemory.getWinHwnd("");
        HWND window = null;
        for (HWND l : list) {
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(l, windowText, 512);
            String wText = Native.toString(windowText).trim();
            if (wText.contains("RotMGExalt")) {
                System.out.println(wText);
                window = l;
            }
        }

        if (window == null) return;

        int id = GHMemory.getWindowPID(window);

//        String dll = "D:\\Programmering\\C++\\repos\\dmgInject\\x64\\Debug\\dmgInject.dll";
        File dll2 = new File("dmgInject.dll");
//        System.out.println(dll2.getAbsolutePath());

        try {
//            boolean injectResult = inject(id, dll);
            boolean injectResult = inject(id, dll2.getAbsolutePath());

            if (injectResult)
                System.out.println("Injection successful!");
            else
                System.out.println("Injection failed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean inject(int processID, String dllName) {
        DWORD_PTR processAccess = new DWORD_PTR(0x43A);

        HANDLE hProcess = kernel32.OpenProcess(processAccess, new BOOL(false), new DWORD_PTR(processID));
        if (hProcess == null) {
            System.out.println("Handle was NULL! Error: " + kernel32.GetLastError());
            return false;
        }

        DWORD_PTR loadLibraryAddress = kernel32.GetProcAddress(kernel32.GetModuleHandle("KERNEL32"), "LoadLibraryA");
        if (loadLibraryAddress.intValue() == 0) {
            System.out.println("Could not find LoadLibrary! Error: " + kernel32.GetLastError());
            return false;
        }

        LPVOID dllNameAddress = kernel32.VirtualAllocEx(hProcess, null, (dllName.length() + 1), new DWORD_PTR(0x3000), new DWORD_PTR(0x4));
        if (dllNameAddress == null) {
            System.out.println("dllNameAddress was NULL! Error: " + kernel32.GetLastError());
            return false;
        }

        Pointer m = new Memory(dllName.length() + 1);
        m.setString(0, dllName);

        boolean wpmSuccess = kernel32.WriteProcessMemory(hProcess, dllNameAddress, m, dllName.length(), null).booleanValue();
        if (!wpmSuccess) {
            System.out.println("WriteProcessMemory failed! Error: " + kernel32.GetLastError());
            return false;
        }

        DWORD_PTR threadHandle = kernel32.CreateRemoteThread(hProcess, 0, 0, loadLibraryAddress, dllNameAddress, 0, 0);
        if (threadHandle.intValue() == 0) {
            System.out.println("threadHandle was invalid! Error: " + kernel32.GetLastError());
            return false;
        }

        kernel32.CloseHandle(hProcess);

        return true;
    }

    public interface Kernel32 extends StdCallLibrary {

        HANDLE OpenProcess(DWORD_PTR dwDesiredAccess, BOOL bInheritHandle, DWORD_PTR dwProcessId);

        DWORD_PTR GetProcAddress(HANDLE hModule, String lpProcName);

        LPVOID VirtualAllocEx(HANDLE hProcess, LPVOID lpAddress, int dwSize, DWORD_PTR flAllocationType, DWORD_PTR flProtect);

        BOOL WriteProcessMemory(HANDLE hProcess, LPVOID lpBaseAddress, Pointer lpBuffer, int nSize, Pointer lpNumberOfBytesWritten);

        DWORD_PTR CreateRemoteThread(HANDLE hProcess, int lpThreadAttributes, int dwStackSize, DWORD_PTR loadLibraryAddress, LPVOID lpParameter, int dwCreationFlags, int lpThreadId);

        BOOL CloseHandle(HANDLE hObject);

        //public HANDLE GetModuleHandleW(WString lpModuleName);
        int GetLastError();

        HANDLE GetModuleHandle(String string);

    }

    public interface PsapiExt extends StdCallLibrary {
        BOOL EnumProcessModulesEx(HANDLE hProcess, DWORD[] hMods, int i, Pointer lpcbNeeded, DWORD dwFilterFlag);

        DWORD GetModuleFileNameEx(HANDLE hProcess, DWORD hModule, char[] szModName, DWORD nSize);
    }
}
