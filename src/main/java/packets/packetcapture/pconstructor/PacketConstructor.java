package packets.packetcapture.pconstructor;

import org.pcap4j.packet.TcpPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.TickAligner;

import java.nio.ByteBuffer;

/**
 * Packet constructor sending the TCP packets to the stream constructor that in turn sends the
 * ordered packets to the rotmg constructor. The packets are then sent back to be decrypted with
 * an RC4 cipher. If a tick packet is sent an aligner is used to check the RC4 alignment using a
 * simple increment from the previous tick packet. If a new session packet is received then it
 * resets the cipher.
 */
public class PacketConstructor implements PConstructor, PReset {

    private final RC4 rc4Cipher;
    private final PacketProcessor packetProcessor;
    private final ROTMGPacketConstructor rotmgConst;
    private final StreamConstructor streamConst;
    private final TickAligner tickAligner;

    /**
     * Packet constructor with specific cipher.
     *
     * @param pp Parent class to send constructed packets back too.
     * @param r  The cipher used to decode packets.
     */
    public PacketConstructor(PacketProcessor pp, RC4 r) {
        packetProcessor = pp;
        rc4Cipher = r;
        rotmgConst = new ROTMGPacketConstructor(this);
        streamConst = new StreamConstructor(this, rotmgConst);
        tickAligner = new TickAligner(rc4Cipher);
    }

    /**
     * Build method to send the packets retrieved by the sniffer for constructing.
     *
     * @param packet Raw TCP packets incoming from the net tap.
     */
    @Override
    public void build(TcpPacket packet) {
        streamConst.build(packet);
    }

    /**
     * Rotmg packets constructed by the rotmg constructor are sent back after they
     * are correctly assembled. If the cipher is correctly aligned then the packets
     * are decrypted and sent to the packet processor. If the cipher isn't aligned
     * then the Tick packets are used to re-align the cipher.
     *
     * @param encryptedData Encrypted packets for aligning cipher and decryption.
     */
    public void packetReceived(ByteBuffer encryptedData) {
        try {
            int size = encryptedData.getInt();
            byte type = encryptedData.get();

            boolean sync = tickAligner.checkRC4Alignment(encryptedData, size, type);

            if (sync) {
                rc4Cipher.decrypt(5, encryptedData);
                packetProcessor.processPackets(type, size, encryptedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset method to reset both cipher and the aligner tick counter when a reset packet is received.
     */
    public void reset() {
        rc4Cipher.reset();
        tickAligner.reset();
        rotmgConst.reset();
    }

    /**
     * Resets needed for starting the sniffer.
     */
    public void startResets() {
        rotmgConst.startResets();
    }
}
