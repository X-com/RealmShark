package pipe;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import example.ExampleModTomato;
import example.damagecalc.DamageLogger;
import example.damagecalc.RNG;

import java.util.Arrays;

public class PipeProducer {

    public static void main(String[] args) {
        createPipe();
    }

    public static void createPipe() {
        String pipeName = "\\\\.\\pipe\\pipeRealmDmg";

        WinNT.HANDLE hNamedPipe = NamedPipe.createPipe(pipeName);
        if (hNamedPipe == null) return;

//        if (!Kernel32.INSTANCE.ConnectNamedPipe(hNamedPipe, null)) return;
//        System.out.println("3");

        new Thread(() -> threadCapture(hNamedPipe)).start();
    }

    public static void threadCapture(WinNT.HANDLE hNamedPipe) {
        byte[] inData = new byte[1000];
        IntByReference bytesRead = new IntByReference();
        int bufferSize = 1000;

        try {
            Thread.sleep(1000);

            while (true) {
                Thread.sleep(100);
                Kernel32.INSTANCE.ConnectNamedPipe(hNamedPipe, null);
                boolean success = Kernel32.INSTANCE.ReadFile(hNamedPipe, // handle to pipe
                        inData, // buffer to receive data
                        bufferSize, // size of buffer
                        bytesRead, // number of bytes read
                        null); // not overlapped I/O

//                System.out.printf("datalen: %d bufsize: %d bytesred: %d\n", inData.length, bufferSize, bytesRead.getValue());
                if (success) {
                    parseData(inData, bytesRead.getValue());
//                    System.out.println(Arrays.toString(inData));
                } else {
                    int error = Kernel32.INSTANCE.GetLastError();
                    System.out.println(error);
                    if (error == 109) {
                        Kernel32.INSTANCE.DisconnectNamedPipe(hNamedPipe);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Kernel32.INSTANCE.FlushFileBuffers(hNamedPipe);
            Kernel32.INSTANCE.DisconnectNamedPipe(hNamedPipe);
            Kernel32.INSTANCE.CloseHandle(hNamedPipe);
        }
    }

    static RNG randy = null;

    static void parseData(byte[] inData, int bytesRead) {
        int index = 0;
        while (index < bytesRead) {
            if (inData[index] == -86 && inData[index + 1] == 85) {
//                System.out.println("num:" + inData[index + 2]);
//                System.out.println("dmg:" + decodeInt(inData, index + 3));
//                System.out.println("time:" + decodeInt(inData, index + 7));
//                System.out.println("id:" + decodeShort(inData, index + 11));
//                int countIndex = inData[index + 2];
//                int dmg = decodeInt(inData, index + 3);
//                int time = decodeInt(inData, index + 7);
//                int id = decodeShort(inData, index + 11);
                byte[] log = {0, 0, 0, 24, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(inData, index + 2, log, 5, 19);
                if (ExampleModTomato.isRunning()) DamageLogger.logDmg(Arrays.toString(log));

                if (inData[index + 2] == 0) {
//                    System.out.println(Arrays.toString(log));
                } else if (inData[index + 2] == 1) {
//                    System.out.println(Arrays.toString(log));
//                    long r = decodeInt(inData, index + 3);
//                    if (randy == null) {
//                        randy = new RNG(r);
//                    } else {
//                        long rr = randy.next();
//                        System.out.println("rng: " + r + " randy: " + rr + "dif: " + (r - rr));
//                        if((r - rr) != 0) {
//                            System.out.println("reseting");
//                            randy = new RNG(r);
//                        }
//                    }
//                    System.out.println(Arrays.toString(log));
//                    System.out.println("rng:" + r);
//                    System.out.println("id:" + decodeShort(inData, index + 7));
                }
                index += 25;
            } else {
                System.out.println("failed: " + index);
                System.out.println(Arrays.toString(inData));
                System.out.println(inData[index]);
            }
            index++;
        }
    }

    public static int decodeInt(byte[] bytes, int offset) {
        return (Byte.toUnsignedInt(bytes[3 + offset]) << 24) | (Byte.toUnsignedInt(bytes[2 + offset]) << 16) | (Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]);
    }

    public static int decodeShort(byte[] bytes, int offset) {
        return ((Byte.toUnsignedInt(bytes[1 + offset]) << 8) | Byte.toUnsignedInt(bytes[offset]));
    }
}
