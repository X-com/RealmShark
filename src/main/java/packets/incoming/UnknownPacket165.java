package packets.incoming;

import packets.Packet;
import packets.reader.BufferReader;

import javax.swing.plaf.synth.SynthUI;

/**
 * Unknown packet -91 / 165
 */
public class UnknownPacket165 extends Packet {
    /**
     * Unknown byte
     */
    public String unknownString;

    @Override
    public void deserialize(BufferReader buffer) throws Exception {
        unknownString = buffer.readString();
    }

    @Override
    public String toString() {
        return "UnknownPacket165{" +
                "\n   unknownString=" + unknownString;
    }
}
