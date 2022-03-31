package packets.packetcapture.encryption;

import packets.PacketType;
import externaltools.HackyPacketLoggerForABug;
import util.Util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The RC4 cipher can be aligned using Tick packets. The packets send ticks regularly
 * from the server incremented by one between each tick packet. The aligner first aligns
 * the RC4 cipher using a simple brute force check from two consecutive tick packets.
 * If the tick counts miss matches relative to next tick packet, it clears all alignments
 * and re-aligns.
 */
public class TickAligner {

    private boolean synced = false;
    private int packetBytes = 0;
    private RC4 rc4;
    private byte[] TickA;
    private int CURRENT_TICK;

    /**
     * Tick aligner constructor to a RC4 cipher.
     *
     * @param r The cipher to align.
     */
    public TickAligner(RC4 r) {
        rc4 = r;
    }

    /**
     * A comprehensive method that keeps track of tick packets and ensures each tick paket
     * is aligned correctly against the CURRENT_TICK index counter. When tick packets arrive
     * they are expected to have the same number as CURRENT_TICK + 1. If miss match is found
     * an error is thrown. The cipher is reset and a brute force method is used to re-align
     * the cipher with two consecutive tick packets.
     *
     * @param encryptedData Data of the current receiving packet.
     * @param size          Size of the packet data.
     * @param type          Type of the packet
     * @return Returns the state of the cipher alignment being synced.
     */
    public boolean checkRC4Alignment(ByteBuffer encryptedData, int size, byte type) {
        if (synced) {
            if (type == PacketType.NEWTICK.getIndex() || type == PacketType.MOVE.getIndex()) {
                byte[] duplicate = Arrays.copyOfRange(encryptedData.array(), 5, encryptedData.capacity());
                rc4.fork().decrypt(duplicate);
                CURRENT_TICK++;
                int tick = Util.decodeInt(duplicate);
                if (CURRENT_TICK != tick) {
                    Util.print("Timeline synchronization critical failure, got: " + tick + " expected: " + CURRENT_TICK);
                    HackyPacketLoggerForABug.dumpData();
                    rc4.reset();
                    synced = false;
                    TickA = null;
                }
            }
            if (synced) packetBytes += size - 5;
        }

        if (!synced) {
            if (type == PacketType.NEWTICK.getIndex() || type == PacketType.MOVE.getIndex()) {
                byte[] tick = Arrays.copyOfRange(encryptedData.array(), 5, 5 + 4);
                if (TickA != null) {
                    rc4.reset();
                    System.out.println("Packets between ticks: " + packetBytes);
                    int i = RC4Aligner.syncCipher(rc4, TickA, tick, packetBytes);
                    if (i != -1) {
                        synced = true;
                        rc4.skip(packetBytes).decrypt(tick);
                        rc4.skip(size - 5 - 4);
                        CURRENT_TICK = Util.decodeInt(tick);
                        System.out.println("Synced. offset: " + i + " tick: " + CURRENT_TICK);
                    } else {
                        Util.print("Time Sync Failed");
                    }
                    TickA = null;
                    packetBytes = 0;
                } else {
                    TickA = tick;
                    packetBytes = size - 5;
                }
            } else {
                packetBytes += size - 5;
            }
            return false;
        }
        return true;
    }

    /**
     * A reset method for resenting the tick counter. Called when changing game sessions.
     */
    public void reset() {
        CURRENT_TICK = -1;
    }
}
