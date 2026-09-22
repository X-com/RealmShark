package packets.incoming;

import packets.reader.BufferReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NewTickPacketSelfCheck {
    public static void main(String[] args) throws Exception {
        ByteBuffer payload = ByteBuffer.allocate(33).order(ByteOrder.BIG_ENDIAN);
        payload.putInt(7);
        payload.putInt(50);
        payload.putInt(1000);
        payload.putShort((short) 25);
        payload.putShort((short) 1);
        payload.put((byte) 42);
        payload.putFloat(1.0f);
        payload.putFloat(2.0f);
        payload.put((byte) 1);
        payload.put((byte) 78);
        payload.putShort((short) 3).put((byte) 'a').put((byte) 'b').put((byte) 'c');
        payload.put((byte) 0);
        payload.flip();

        NewTickPacket packet = new NewTickPacket();
        BufferReader reader = new BufferReader(payload);
        packet.deserialize(reader);

        if (
                packet.tickId != 7
                        || packet.tickTime != 50
                        || packet.serverRealTimeMS != 1000
                        || packet.serverLastTimeRTTMS != 25
                        || packet.status.length != 1
                        || packet.status[0].stats[0].statTypeNum != 78
                        || !"abc".equals(packet.status[0].stats[0].stringStatValue)
                        || !reader.isBufferFullyParsed()
        ) {
            throw new AssertionError("NewTick stat 78 parse failed");
        }
    }
}
