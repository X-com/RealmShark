package assembly;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.TOKEN_PRIVILEGES;
import com.sun.jna.ptr.IntByReference;

public class GHTools {

    //normal sleep with error handeling
    public static boolean sleep(int time){
        try {
            Thread.sleep(time);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    //get PID of window:
    public static int getGamePID(){
        IntByReference PID = new IntByReference(0);
        User32.INSTANCE.GetWindowThreadProcessId(GHMemory.getHandle(), PID);
        return PID.getValue();
    }

    //enable debug privelege
    public static boolean enableProcessDebug() {
        boolean result = false;
        HANDLEByReference hToken = new HANDLEByReference();
        HANDLE hProcess = Kernel32.INSTANCE.GetCurrentProcess();

        if(!Advapi32.INSTANCE.OpenProcessToken(hProcess,
                WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY, hToken)){
            return false;
        }


        TOKEN_PRIVILEGES tkp = new TOKEN_PRIVILEGES(1024);
        IntByReference returnLength = new IntByReference();
        if(Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(), WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges, tkp, tkp.size(), returnLength)){
            if(tkp.PrivilegeCount.intValue() < 1){
                return false;
            }

            WinNT.LUID luid = null;

            for (int i=0; i<tkp.PrivilegeCount.intValue(); i++) {
                if ((tkp.Privileges[i].Attributes.intValue() & WinNT.SE_PRIVILEGE_ENABLED) > 0) {
                    luid = tkp.Privileges[i].Luid;
                }
            }
            if(luid == null){
                return false;
            }
            if(!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)){
                return false;
            }

            tkp = new WinNT.TOKEN_PRIVILEGES(1);
            tkp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid,new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
            if(Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, tkp, 0, null, null)){
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
                return true;
            }


        }
        return result;
    }

    //return if game window is visible or not
    public static boolean isGameVisible() {
        return User32.INSTANCE.IsWindowVisible(GHMemory.getHandle());
    }

    //get height of the Game Window
    public static int getGameHeight() {
        //rect of GameWindow:
        RECT rect = new RECT();
        boolean res = User32.INSTANCE.GetWindowRect(GHMemory.getHandle(), rect);

        if(res) {
            return rect.bottom-rect.top;
        }
        return -1;
    }

    public static int getGameXPos() {
        //rect of GameWindow:
        RECT rect = new RECT();
        boolean res = User32.INSTANCE.GetWindowRect(GHMemory.getHandle(), rect);

        if(res) {
            return rect.left;
        }
        return -1;
    }

    public static int getGameYPos() {
        //rect of GameWindow:
        RECT rect = new RECT();
        boolean res = User32.INSTANCE.GetWindowRect(GHMemory.getHandle(), rect);

        if(res) {
            return rect.top;
        }
        return -1;
    }

    public static int getGameWidth() {
        //rect of GameWindow:
        RECT rect = new RECT();
        boolean res = User32.INSTANCE.GetWindowRect(GHMemory.getHandle(), rect);

        if(res) {
            return rect.right-rect.left;
        }
        return -1;
    }
}
