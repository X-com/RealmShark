package packets.incoming.ip;

import packets.Packet;
import packets.reader.BufferReader;
import util.Util;

import java.util.Arrays;

/**
 * Emits this class when IP changes happen on incoming packets.
 */
public class IpAddress extends Packet {

    /**
     * IP of incoming packets
     */
    public byte[] srcAddress;
    /**
     * IP of incoming packets as a single integer
     */
    public int srcAddressAsInt;

    public IpAddress() {
    }

    public IpAddress(byte[] srcAddr) {
        super();
        srcAddress = srcAddr;
        srcAddressAsInt = Util.decodeInt(srcAddr);
    }

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
    }

    @Override
    public String toString() {
        return "IpAddress{" +
                "\n   srcAddress=" + Arrays.toString(srcAddress) +
                "\n   srcAddressAsInt=" + srcAddressAsInt;
    }
}
