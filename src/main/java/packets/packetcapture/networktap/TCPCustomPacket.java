package packets.packetcapture.networktap;

public class TCPCustomPacket {
    private final int identifier, portSrc, portDst;
    private final byte[] rawBytes, tcpBytes;

    public TCPCustomPacket(int i, int s, int d, byte[] b, byte[] tb) {
        identifier = i;
        portSrc = s;
        portDst = d;
        rawBytes = b;
        tcpBytes = tb;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getPortSrc() {
        return portSrc;
    }

    public int getPortDst() {
        return portDst;
    }

    public int length() {
        return tcpBytes.length;
    }

    public byte[] tcpData() {
        return tcpBytes;
    }

    public byte[] getRawData() {
        return rawBytes;
    }
}
