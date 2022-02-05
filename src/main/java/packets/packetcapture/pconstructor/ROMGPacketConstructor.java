package packets.packetcapture.pconstructor;

import jpcap.packet.TCPPacket;
import util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Rotmg packet constructor appending bytes into a packet based on the size at the header of each sequence.
 */
public class ROMGPacketConstructor implements PConstructor {

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
     * Build method used to stitch individual bytes in the data in the TCP packets according to
     * specified size at the header of the data.
     *
     * @param packetSequenced TCP packet with the data inside.
     *                        <p>
     *                        TODO: Fix larger packets throwing of the packet size.
     *                        Only start listen after the next packet less than MTU(maximum
     *                        trasmition unit packet) is received.
     */
    @Override
    public void build(TCPPacket packetSequenced) {
        for (byte b : packetSequenced.data) {
            bytes[index++] = b;
            if (index >= 4) {
                int psize = Util.decodeInt(bytes);
                if (index == psize) {
                    index = 0;
                    byte[] data = Arrays.copyOfRange(bytes, 0, psize);
                    ByteBuffer packetData = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
                    packetConstructor.packetReceived(packetData);
                }
            }
        }
    }
}
