package pipe;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

public class NamedPipe {

    public static WinNT.HANDLE createPipe(String pipeName) {
        int bufferSize = 1000000;
        WinNT.HANDLE hNamedPipe = Kernel32.INSTANCE.CreateNamedPipe(pipeName,
                WinBase.PIPE_ACCESS_DUPLEX,                                                 // dwOpenMode
                WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT,    // dwPipeMode
                WinBase.PIPE_UNLIMITED_INSTANCES,                                           // nMaxInstances,
                bufferSize,                                                                 // nOutBufferSize,
                bufferSize,                                                                 // nInBufferSize,
                1000,                                                                       // nDefaultTimeOut,
                null);                                                                      // lpSecurityAttributes

        if (WinBase.INVALID_HANDLE_VALUE.equals(hNamedPipe)) {
            System.out.println("FAIL: " + hNamedPipe);
            return null;
        }

        return hNamedPipe;
    }
}
