package packets.incoming.depricated;

import packets.Packet;
import packets.buffer.PBuffer;

public class Ping extends Packet {
    public int serial;

    @Override
    public void deserialize(PBuffer buffer) {
        serial = buffer.readInt();
    }
}
