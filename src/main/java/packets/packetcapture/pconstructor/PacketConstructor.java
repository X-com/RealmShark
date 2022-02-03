package packets.packetcapture.pconstructor;

import jpcap.packet.TCPPacket;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.encryption.TickAligner;
import packets.packetcapture.encryption.RC4;

import java.nio.ByteBuffer;

/**
 * Packet constructor sending the TCP packets to the stream constructor that in turn sends the
 * ordered packets to the rotmg constructor. The packets are then sent back to be decrypted with
 * an RC4 cipher. If a tick packet is sent an aligner is used to check the RC4 alignment using a
 * simple increment from the previous tick packet. If a new session packet is received then it
 * resets the cipher.
 */
public class PacketConstructor implements PConstructor, PReset {

    RC4 rc4Cipher;
    PacketProcessor packetProcessor;
    ROMGPacketConstructor romgpConst;
    StreamConstructor streamConst;
    TickAligner tickAligner;

    /**
     * Packet constructor constructor with specific cipher.
     * @param pp Parrent class to send constructed packets back too.
     * @param r The cipher used to decode packets.
     */
    public PacketConstructor(PacketProcessor pp, RC4 r) {
        packetProcessor = pp;
        rc4Cipher = r;
        romgpConst = new ROMGPacketConstructor(this);
        streamConst = new StreamConstructor(this, romgpConst);
        tickAligner = new TickAligner(rc4Cipher);
    }

    /**
     * Build method to send the packets retrieved by the sniffer for constructing.
     *
     * @param packet Raw TCP packets incoming from the net tap.
     */
    @Override
    public void build(TCPPacket packet) {
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
            int size = encryptedData.getInt(0);
            byte type = encryptedData.get(4);

            boolean sync = tickAligner.checkRC4Alignment(encryptedData, size, type);

            if (sync) {
                rc4Cipher.decrypt(5, encryptedData);
                packetProcessor.processPackets(type, encryptedData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reset method to reset both cipher and the aligner tick counter when a reset packet is received.
     */
    public void reset() {
        rc4Cipher.reset();
        tickAligner.reset();
    }
}
