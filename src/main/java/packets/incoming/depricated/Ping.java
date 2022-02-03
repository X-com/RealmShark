package packets.incoming.depricated;

import packets.buffer.PBuffer;
import packets.Packet;

public class Ping extends Packet {
    public int serial;

    @Override
    public void deserialize(PBuffer buffer) {
        serial = buffer.readInt();
    }
}
