package packets.outgoing;

import packets.Packet;
import packets.reader.BufferReader;

/**
 * Packet confirmed Kensei dash to specific coordinates
 */
public class DashPacket extends Packet {
    /**
     * The current client time.
     */
    public int time;
    /**
     * unknown ints
     */
    public float startX;
    public float startY;
    public float endX;
    public float endY;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        time = buffer.readInt();
        startX = buffer.readFloat();
        startY = buffer.readFloat();
        endX = buffer.readFloat();
        endY = buffer.readFloat();
    }

    @Override
    public String toString() {
        return "DashPacket{" +
                "\n   time=" + time +
                "\n   startX=" + startX +
                "\n   startY=" + startY +
                "\n   endX=" + endX +
                "\n   endY=" + endY;
    }
}
