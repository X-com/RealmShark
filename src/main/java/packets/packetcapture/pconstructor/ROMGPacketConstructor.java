package packets.packetcapture.pconstructor;

import jpcap.packet.TCPPacket;
import util.HackyPacketLoggerForABug;
import util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Rotmg packet constructor appending bytes into a packet based on the size at the header of each sequence.
 */
public class ROMGPacketConstructor implements PConstructor {

    private boolean firstNonLargePacket;
    PacketConstructor packetConstructor;
    byte[] bytes = new byte[10000000];
    int index;

    /**
     * ROMGPacketConstructor needing the PacketConstructor class to send correctly stitched packets.
     *
     * @param pc PacketConstructor class needed to send correctly stitched packets.
     */
    public ROMGPacketConstructor(PacketConstructor pc) {
        packetConstructor = pc;
    }

    /**
     * Reset when starting the sniffer. Given the program can start at any time then any packet which
     * follows a non-max packet will most likely contain the rotmg-packet header which contains the
     * packet size. If ignoring this flag, any random MTU(maximum transmission unit packet) packet in
     * a sequence of concatenated packets could produce a random packet size from its first 4 bytes
     * resulting in a de-sync.
     */
    public void startResets() {
        firstNonLargePacket = true;
    }

    /**
     * Build method used to stitch individual bytes in the data in the TCP packets according to
     * specified size at the header of the data.
     * Only start listen after the next packet less than MTU(maximum transmission unit packet) is received.
     *
     * @param packetSequenced TCP packet with the data inside.
     */
    @Override
    public void build(TCPPacket packetSequenced) {
        if (firstNonLargePacket) { // start listening after a non-max packet
            // prevents errors in pSize.
            if (packetSequenced.data.length < 1460) firstNonLargePacket = false;
            return;
        }
        HackyPacketLoggerForABug.logTCPPacket(packetSequenced); // TEMP logger to find a bug
        int pSize = 0;
        for (byte b : packetSequenced.data) {
            bytes[index++] = b;
            if (index >= 4) {
                if (pSize == 0) pSize = Util.decodeInt(bytes);

                if (index == pSize) {
                    index = 0;
                    byte[] data = Arrays.copyOfRange(bytes, 0, pSize);
                    pSize = 0;
                    ByteBuffer packetData = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
                    packetConstructor.packetReceived(packetData);
                }
            }
        }
    }
}
