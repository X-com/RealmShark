package assembly;

import com.sun.jna.platform.KeyboardUtils;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.platform.win32.WinUser.INPUT;

public class GHInput {

    //checks if button is pressed
    public static boolean getKeyDown(int key) {
        return KeyboardUtils.isPressed(key);
    }

    //sends key input
    public static void sendKeyPress(int key) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wScan = new WORD(0);
        input.input.ki.time = new DWORD(0);
        input.input.ki.dwExtraInfo = new ULONG_PTR(0);
        input.input.ki.wVk = new WORD(key);
        input.input.ki.dwFlags = new DWORD(0);
        User32.INSTANCE.SendInput(new DWORD(1), (INPUT[]) input.toArray(1), input.size());
        input.input.ki.wVk = new WORD(key);
        input.input.ki.dwFlags = new DWORD(2);
        User32.INSTANCE.SendInput(new DWORD(1), (INPUT[]) input.toArray(1), input.size());
    }

    //press key down input
    public static void sendKeyDown(int key) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wScan = new WORD(0);
        input.input.ki.time = new DWORD(0);
        input.input.ki.dwExtraInfo = new ULONG_PTR(0);
        input.input.ki.wVk = new WORD(key);
        input.input.ki.dwFlags = new DWORD(0);
        User32.INSTANCE.SendInput(new DWORD(1), (INPUT[]) input.toArray(1), input.size());
    }

    //let key up input
    public static void sendKeyUp(int key) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wScan = new WORD(0);
        input.input.ki.time = new DWORD(0);
        input.input.ki.dwExtraInfo = new ULONG_PTR(0);
        input.input.ki.wVk = new WORD(key);
        input.input.ki.dwFlags = new DWORD(2);
        User32.INSTANCE.SendInput(new DWORD(1), (INPUT[]) input.toArray(1), input.size());
    }

    //set cursor position to point somewhere
    public void SetCursor(int x, int y) {
        User32.INSTANCE.SetCursorPos(x, y);
    }

    //where does the cursor currently point to?
    public int[] getCursorPos() {
        POINT p = new POINT();
        User32.INSTANCE.GetCursorPos(p);
        return new int[]{p.x, p.y};
    }
}
