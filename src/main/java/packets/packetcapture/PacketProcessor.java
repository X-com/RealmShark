package packets.packetcapture;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.TcpPacket;
import packets.Packet;
import packets.reader.BufferReader;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.RotMGRC4Keys;
import packets.packetcapture.networktap.Sniffer;
import packets.packetcapture.networktap.WindowsSniffer;
import packets.packetcapture.pconstructor.PConstructor;
import packets.packetcapture.pconstructor.PacketConstructor;
import packets.packetcapture.register.Register;
import packets.PacketType;
import util.Util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The core class to process packets. First the network tap is sniffed to receive all packets. The packets
 * are filtered for port 2050, the rotmg port, and TCP packets. Then the packets are stitched together in
 * streamConstructor and rotmgConstructor class. After the packets are constructed the RC4 cipher is used
 * decrypt the data. The data is then matched with target classes and emitted through the registry.
 */
public class PacketProcessor extends Thread {
    PConstructor incomingPacketConstructor;
    PConstructor outgoingPacketConstructor;
    Sniffer sniffer;

    /**
     * Basic constructor of packetProcessor
     * TODO: Add linux and mac support later
     */
    public PacketProcessor() {
        sniffer = new WindowsSniffer(this);
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
     * Stop method for PacketProcessor.
     */
    public void stopSniffer() {
        sniffer.closeSniffers();
    }

    /**
     * Method to start the packet sniffer that will send packets back to receivedPackets.
     */
    public void tapPackets() {
        incomingPacketConstructor.startResets();
        outgoingPacketConstructor.startResets();
        try {
            sniffer.startSniffer();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for retrieving TCP packets incoming from the sniffer.
     *
     * @param packet The TCP packets retrieved from the network tap.
     */
    public void receivedPackets(TcpPacket packet) {
        // 2050 is default rotmg server port. Incoming packets have 2050 source port.
        if (packet.getHeader().getSrcPort().value() == 2050) {
            constIncomingPackets(packet);

            // Outgoing packets have destination port set to 2050.
        } else if (packet.getHeader().getDstPort().value() == 2050) {
//            constOutgoingPackets(packet); // removed given it is not implemented properly
        }
    }

    /**
     * Incoming packets from rotmg servers.
     *
     * @param packet Incoming TCP packet
     */
    private void constIncomingPackets(TcpPacket packet) {
        incomingPacketConstructor.build(packet);
    }

    /**
     * Outgoing packets to rotmg servers.
     *
     * @param packet Outgoing TCP packet
     */
    private void constOutgoingPackets(TcpPacket packet) {
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
        if (!PacketType.containsKey(type)) {
            System.err.println("Unknown packet type:" + type + " Data:" + Arrays.toString(data.array()));
            return;
        }
        Packet packetType = PacketType.getPacket(type).factory();
        BufferReader pData = new BufferReader(data);

        System.out.println("packets " + type);
        try {
            packetType.deserialize(pData);
            pData.bufferFullyParsed(PacketType.byOrdinal(type), packetType);
        } catch (Exception e) {
            debugPackets(type, data);
            return;
        }
        Register.INSTANCE.emit(packetType);
    }

    /**
     * Helper for debugging packets
     */
    private void debugPackets(int type, ByteBuffer data) {
        Packet packetType = PacketType.getPacket(type).factory();
        try {
            Util.print("Debugging packet: " + PacketType.byOrdinal(type));
            data.position(5);
            BufferReader pDebug = new BufferReader(data);
            pDebug.printError(PacketType.byOrdinal(type), packetType);
            packetType.deserialize(pDebug);
        } catch (Exception e) {
            Util.print(Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"));
        }
    }
}
