package packets.packetcapture.sniff.netpackets;

import java.time.Instant;
import java.util.Arrays;

public class RawPacket {

    private final Instant instant;
    private final int payloadSize;
    private final byte[] payload;

    public static RawPacket newPacket(byte[] rawData, Instant ts) {
        return new RawPacket(rawData, ts);
    }

    public RawPacket(byte[] data, Instant ins) {
        instant = ins;
        payloadSize = data.length;
        payload = data;
    }

    public Instant getInstant() {
        return instant;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public byte[] getPayload() {
        return payload;
    }

    public EthernetPacket getNewEthernetPacket() {
        return new EthernetPacket(payload);
    }

    @Override
    public String toString() {
        return "RawPacket{" +
                "\n  instant=" + instant +
                "\n, payloadSize=" + payloadSize +
                "\n, payload=" + Arrays.toString(payload) +
                "\n}";
    }
}
