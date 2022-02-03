package packets.packetcapture;

import jpcap.packet.TCPPacket;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.RotMGRC4Keys;
import packets.packetcapture.networktap.Sniffer;
import packets.packetcapture.networktap.WindowsSniffer;
import packets.packetcapture.pconstructor.PConstructor;
import packets.packetcapture.pconstructor.PacketConstructor;
import packets.packetcapture.register.Register;
import packets.buffer.PBuffer;
import packets.Packet;
import packets.packetcapture.type.PacketTypes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The core class to process packets. First the network tap is sniffed to receive all packets. The packets
 * are filtered for port 2050, the rotmg port, and TCP packets. Then the packets are stitched together in
 * streamConstructor and rotmgConstructor class. After the packets are constructed the RC4 cipher is used
 * decrypt the data. The data is then matched with target classes and emitted through the registry.
 */
public class PacketProcessor {
    PConstructor incomingPacketConstructor;
    PConstructor outgoingPacketConstructor;
    Sniffer windowsSniffer;

    /**
     * Basic constructor of packetProcessor
     */
    public PacketProcessor() {
        windowsSniffer = new WindowsSniffer(this);
        incomingPacketConstructor = new PacketConstructor(this, new RC4(RotMGRC4Keys.INCOMING_STRING));
        outgoingPacketConstructor = new PacketConstructor(this, new RC4(RotMGRC4Keys.OUTGOING_STRING));
    }

    /**
     * Start method for PacketProcessor.
     */
    public void run() {
        tapPackets();
    }

    /**
     * Method to start the packet sniffer that will send packets back to receivedPackets.
     *
     * TODO: Add linux and mac support later
     */
    public void tapPackets() {
        try {
            windowsSniffer.startSniffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for retrieving TCP packets incoming from the sniffer.
     *
     * @param packet The TCP packets retrieved from the network tap.
     */
    public void receivedPackets(TCPPacket packet) {
        // 2050 is default rotmg server port. Incoming packets have 2050 source port.
        if (packet.src_port == 2050) {
            constIncomingPackets(packet);
        } else { // Outgoing packets have destination port set to 2050.
            constOutgoingPackets(packet);
        }
    }

    /**
     * Incoming packets from rotmg servers.
     *
     * @param packet Incoming TCP packet
     */
    private void constIncomingPackets(TCPPacket packet) {
        incomingPacketConstructor.build(packet);
    }

    /**
     * Outgoing packets to rotmg servers.
     *
     * @param packet Outgoing TCP packet
     */
    private void constOutgoingPackets(TCPPacket packet) {
        outgoingPacketConstructor.build(packet);
    }

    /**
     * Completed packets constructed by stream and rotmg constructor returned to packet constructor.
     * Decoded by the cipher and sent back to the processor to be emitted to subscribed users.
     *
     * @param type Constructed packet type.
     * @param data Constructed packet data.
     */
    public void processPackets(int type, ByteBuffer data) {
        if (!PacketTypes.containsKey(type)) {
            System.err.println("Unknown packet type:" + type + " Data:" + Arrays.toString(data.array()));
            return;
        }
        Packet packetType = PacketTypes.getPacket(type).factory();
        PBuffer pData = new PBuffer(data);
        packetType.deserialize(pData);
        Register.INSTANCE.emit(packetType);
    }
}
