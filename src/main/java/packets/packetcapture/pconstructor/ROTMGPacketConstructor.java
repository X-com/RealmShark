package packets.packetcapture.pconstructor;

import util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Rotmg packet constructor appending bytes into a packet based on the size at the header of each sequence.
 */
public class ROTMGPacketConstructor {

    private PacketConstructor packetConstructor;
    private byte[] bytes = new byte[200000];
    private int index;
    private int pSize = 0;

    /**
     * ROMGPacketConstructor needing the PacketConstructor class to send correctly stitched packets.
     *
     * @param pc PacketConstructor class needed to send correctly stitched packets.
     */
    public ROTMGPacketConstructor(PacketConstructor pc) {
        packetConstructor = pc;
    }

    /**
     * Build method used to stitch individual bytes in the data in the TCP packets according to
     * specified size at the header of the data.
     * Only start listen after the next packet less than MTU(maximum transmission unit packet) is received.
     *
     * @param data TCP packet with the data inside.
     */
    public void build(byte[] data) {
        for (byte b : data) {
            bytes[index++] = b;
            if (index >= 4) {
                if (pSize == 0) {
                    pSize = Util.decodeInt(bytes);
                    if (pSize > 200000) {
                        Util.print("Oversize packet construction.");
                        pSize = 0;
                        return;
                    }
                }

                if (index == pSize) {
                    index = 0;
                    byte[] realmPacket = Arrays.copyOfRange(bytes, 0, pSize);
                    pSize = 0;
                    ByteBuffer packetData = ByteBuffer.wrap(realmPacket).order(ByteOrder.BIG_ENDIAN);
                    packetConstructor.packetReceived(packetData);
                }
            }
        }
    }

    /**
     * Resets the byte index and the packet size.
     */
    public void reset() {
        index = 0;
        pSize = 0;
    }
}
