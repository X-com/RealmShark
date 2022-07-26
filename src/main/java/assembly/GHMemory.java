package assembly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.WinDef.DWORD;

public class GHMemory {

    private static HWND handle;
    private static HANDLE hProcess;
    public static final long ALL_ACCESS = WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE | WinNT.PROCESS_VM_OPERATION;
    private static final HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(0xFFFFFFFFL));
    private static GHArchitecture arch = GHArchitecture.Win32;

    //call openProcess with only the window name
    public static boolean openProcess(String windowName) {
        try {
            return openProcess(getWinHwnd(windowName).get(0));
        } catch (Exception e) {
            return false;
        }
    }

    //get a handle to the process of the window:
    public static boolean openProcess(HWND window) {

        //if a handle is already open, we need to close it first
        if (hProcess != null) {
            if (!Kernel32.INSTANCE.CloseHandle(hProcess)) {
                //closing handle failed so we can't open a new one
                return false;
            }
        }
        handle = window;
        int pid = getWindowPID(window);
        hProcess = Kernel32.INSTANCE.OpenProcess(new DWORD(ALL_ACCESS).intValue(), false, new DWORD(pid).intValue());

        if (hProcess == null) {
            return false; //no handle was opened
        } else {
            return true; //handle was opened
        }
    }

    //is the game still running are we connected
    public static boolean isConnected() {
        return !(hProcess.equals(null) || User32.INSTANCE.IsWindow(handle) || hProcess.equals(INVALID_HANDLE_VALUE));
    }

    //set architecture to use correct pointers etc.
    public static void setArchitecture(GHArchitecture architecture) {
        arch = architecture;
    }

    //read bit from memory
    public static boolean readBit(long address, int position) {
        Memory mem = readMemory(hProcess, address, 1);
        boolean value = ((mem.getByte(0) >> position) & 1) == 1;
        return value;
    }

    //read byte from memory
    public static byte readByte(long address) {
        Memory mem = readMemory(hProcess, address, 1);
        byte value = mem.getByte(0);
        return value;
    }

    //read short from memory
    public static short readShort(long address) {
        Memory mem = readMemory(hProcess, address, 2);
        short value = mem.getShort(0);
        return value;
    }

    //read char from memory
    public static char readChar(long address) {
        Memory mem = readMemory(hProcess, address, 2);
        char value = mem.getChar(0);
        return value;
    }

    //read char from memory
    public static int readInt(long address) {
        Memory mem = readMemory(hProcess, address, 4);
        int value = mem.getInt(0);
        return value;
    }

    //read long from memory
    public static long readLong(long address) {
        Memory mem = readMemory(hProcess, address, 8);
        long value = mem.getLong(0);
        return value;
    }

    //read float from memory
    public static float readFloat(long address) {
        Memory mem = readMemory(hProcess, address, 4);
        float value = mem.getFloat(0);
        return value;
    }

    //read double from memory
    public static double readDouble(long address) {
        Memory mem = readMemory(hProcess, address, 8);
        double value = mem.getDouble(0);
        return value;
    }

    //read string from memory
    public static String readString(long address, int bytestoread) {
        Memory mem = readMemory(hProcess, address, bytestoread);
        String value = mem.getString(0);
        return value;
    }

    //read other objects from memory
    public static <T> T read(long address, int bytesToRead) throws ClassNotFoundException {
        Memory mem = readMemory(hProcess, address, bytesToRead);
        T value = deserialize(mem.getByteArray(0, bytesToRead));
        return value;
    }

    //read byte array from memory
    public static byte[] readByteArray(long address, int bytesToRead) {
        Memory mem = readMemory(hProcess, address, bytesToRead);
        byte[] value = mem.getByteArray(0, bytesToRead);
        return value;
    }

    //helper method to read data from memory
    public static Memory readMemory(HANDLE proccess, long address, int bytesToRead) {
        IntByReference bytesRead = new IntByReference(0);
        Memory output = new Memory(bytesToRead);
        Kernel32.INSTANCE.ReadProcessMemory(proccess, new Pointer(address), output, bytesToRead, bytesRead);
        return output;
    }

    //write bit back to memory
    public static boolean writeBit(boolean data, long address, int position) {
        Memory mem = readMemory(hProcess, address, 1);
        byte writeBack;

        if (data) {
            writeBack = (byte) (mem.getByte(0) | (1 << position));
        } else {
            writeBack = (byte) (mem.getByte(0) & ~(1 << position));
        }

        mem.setByte(0, writeBack);
        return writeMemory(hProcess, address, mem, 1);
    }

    //write byte back to memory
    public static boolean writeByte(byte data, long address) {
        Memory mem = new Memory(1);
        mem.setByte(0, data);
        return writeMemory(hProcess, address, mem, 1);
    }

    //write short back to memory
    public static boolean writeShort(short data, long address) {
        Memory mem = new Memory(2);
        mem.setShort(0, data);
        return writeMemory(hProcess, address, mem, 2);
    }

    //write char back to memory
    public static boolean writeChar(char data, long address) {
        Memory mem = new Memory(2);
        mem.setChar(0, data);
        return writeMemory(hProcess, address, mem, 2);
    }

    //write byte int to memory
    public static boolean writeInt(int data, long address) {
        Memory mem = new Memory(4);
        mem.setInt(0, data);
        return writeMemory(hProcess, address, mem, 4);
    }

    //write long back to memory
    public static boolean writeLong(long data, long address) {
        Memory mem = new Memory(8);
        mem.setLong(0, data);
        return writeMemory(hProcess, address, mem, 8);
    }

    //write float back to memory
    public static boolean writeFloat(float data, long address) {
        Memory mem = new Memory(4);
        mem.setFloat(0, data);
        return writeMemory(hProcess, address, mem, 4);
    }

    //write double back to memory
    public static boolean writeDouble(double data, long address) {
        Memory mem = new Memory(8);
        mem.setDouble(0, data);
        return writeMemory(hProcess, address, mem, 8);
    }

    //write string back to memory
    public boolean writeString(long address, String string) {
        Memory mem = new Memory(string.getBytes().length);
        mem.setString(0, string);
        return writeMemory(hProcess, address, mem, string.getBytes().length);
    }

    //write byte array back to memory
    public static boolean write(byte[] data, long address) {
        Memory mem = new Memory(data.length);

        for (int i = 0; i < data.length; i++) {
            mem.setByte(i, data[i]);
        }
        return writeMemory(hProcess, address, mem, data.length);
    }

    //Generic method to write data back to memory
    public static <T> boolean write(T data, long address) {
        Memory mem = new Memory(ObjectSizeFetcher.getObjectSize(data));
        byte[] dataBytes = serialize(data);

        for (int i = 0; i < dataBytes.length; i++) {
            mem.setByte(i, dataBytes[i]);
        }
        return writeMemory(hProcess, address, mem, ObjectSizeFetcher.getObjectSize(data));
    }

    //helper method which returns the PID of a Window
    public static int getWindowPID(HWND window) {
        IntByReference PID = new IntByReference(0);
        User32.INSTANCE.GetWindowThreadProcessId(window, PID);
        return PID.getValue();
    }

    public static boolean writeMemory(HANDLE process, long address, Memory write, int size) {
        IntByReference byteswritten = new IntByReference(0);
        return Kernel32.INSTANCE.WriteProcessMemory(process, new Pointer(address), write, size, byteswritten);
    }

    //Convert Objects to byte arrays
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    //Convert byte arrays back to Objects
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            ObjectInputStream is = new ObjectInputStream(in);
            return (T) is.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    //access handle to process
    public static HWND getHandle() {
        return handle;
    }

    //get handle to window by title
    public static ArrayList<HWND> getWinHwnd(final String startOfWindowName) {
        final ArrayList<HWND> hWndC = new ArrayList<HWND>();
        User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
            @Override
            public boolean callback(HWND hWnd, Pointer userData) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
                String wText = Native.toString(windowText).trim();

                if (!wText.isEmpty() /*&& wText.startsWith(startOfWindowName)*/) {
                    hWndC.add(hWnd);
                }
                return true;
            }
        }, null);
        return hWndC;
    }

    //get the dynamic Object address from its static Pointer
    public static long getObjectAddress(GHPointer staticMultiLevelPointer) {

        long objectPointer = staticMultiLevelPointer.getStaticPointer();

        Memory mem = null;

        if (arch == GHArchitecture.Win32) {
            for (int i = 0; i < staticMultiLevelPointer.getOffsets().length; i++) {
                mem = readMemory(hProcess, objectPointer, 4);
                objectPointer = (mem.getInt(0) + staticMultiLevelPointer.getOffsets()[i]);

            }
        } else if (arch == GHArchitecture.Win64) {
            for (int i = 0; i < staticMultiLevelPointer.getOffsets().length; i++) {
                mem = readMemory(hProcess, objectPointer, 8);
                objectPointer = (mem.getLong(0) + staticMultiLevelPointer.getOffsets()[i]);

            }
        }
        return (objectPointer);
    }

    //close handle to process
    public static void close() {
        Kernel32.INSTANCE.CloseHandle(hProcess);
    }
}
