package packets.packetcapture;

import example.gui.MissingNpcapGUI;
import example.gui.TomatoBandwidth;
import packets.Packet;
import packets.PacketType;
import packets.packetcapture.encryption.RC4;
import packets.packetcapture.encryption.RotMGRC4Keys;
import packets.packetcapture.logger.PacketLogger;
import packets.packetcapture.sniff.PProcessor;
import packets.packetcapture.sniff.Sniffer;
import packets.packetcapture.pconstructor.PacketConstructor;
import packets.packetcapture.register.Register;
import packets.reader.BufferReader;
import util.Util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The core class to process packets. First the network tap is sniffed to receive all packets. The packets
 * are filtered for port 2050, the rotmg port, and TCP packets. Then the packets are stitched together in
 * streamConstructor and rotmgConstructor class. After the packets are constructed the RC4 cipher is used
 * decrypt the data. The data is then matched with target classes and emitted through the registry.
 */
public class PacketProcessor extends Thread implements PProcessor {
    private final PacketConstructor incomingPacketConstructor;
    private final PacketConstructor outgoingPacketConstructor;
    private final Sniffer sniffer;
    private final PacketLogger logger;

    /**
     * Basic constructor of packetProcessor
     * TODO: Add linux and mac support later
     */
    public PacketProcessor() {
        sniffer = new Sniffer(this);
        incomingPacketConstructor = new PacketConstructor(this, new RC4(RotMGRC4Keys.INCOMING_STRING));
        outgoingPacketConstructor = new PacketConstructor(this, new RC4(RotMGRC4Keys.OUTGOING_STRING));
        logger = new PacketLogger();
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
        logger.startLogger();
        incomingPacketConstructor.startResets();
        outgoingPacketConstructor.startResets();
        try {
            sniffer.startSniffer();
        } catch (UnsatisfiedLinkError e) {
            new MissingNpcapGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Incoming byte data received from incoming TCP packets.
     *
     * @param data Incoming byte stream
     */
    @Override
    public void incomingStream(byte[] data) {
        logger.addIncoming(data.length);
        incomingPacketConstructor.build(data);
        TomatoBandwidth.setInfo(logger.toString()); // update info GUI if open
    }

    /**
     * Outgoing byte data received from outgoing TCP packets.
     *
     * @param data Outgoing byte stream
     */
    @Override
    public void outgoingStream(byte[] data) {
        logger.addOutgoing(data.length);
        outgoingPacketConstructor.build(data);
        TomatoBandwidth.setInfo(logger.toString()); // update info GUI if open
    }

    /**
     * Completed packets constructed by stream and rotmg constructor returned to packet constructor.
     * Decoded by the cipher and sent back to the processor to be emitted to subscribed users.
     *
     * @param type Constructed packet type.
     * @param size size of the packet.
     * @param data Constructed packet data.
     */
    public void processPackets(byte type, int size, ByteBuffer data) {
        if (!PacketType.containsKey(type)) {
            System.err.println("Unknown packet type:" + type + " Data:" + Arrays.toString(data.array()));
            return;
        }
        logger.addPacket(type, size);
        Packet packetType = PacketType.getPacket(type).factory();
        BufferReader pData = new BufferReader(data);

        try {
            packetType.deserialize(pData);
            if (!pData.isBufferFullyParsed())
                pData.printError(packetType);
        } catch (Exception e) {
            Util.print("Buffer exploded: " + pData.getIndex() + "/" + pData.size());
            debugPackets(type, data);
            return;
        }
        Register.INSTANCE.emit(packetType);

        if(type == PacketType.NEWTICK.getIndex()) return;
        if(type == PacketType.UPDATE.getIndex()) return;
        if(type == PacketType.UPDATEACK.getIndex()) return;
        if(type == PacketType.PING.getIndex()) return;
        if(type == PacketType.PONG.getIndex()) return;
        if(type == PacketType.MOVE.getIndex()) return;
        if(type == PacketType.SHOWEFFECT.getIndex()) return;
        if(type == PacketType.TEXT.getIndex()) return;
        if(type == PacketType.PLAYERTEXT.getIndex()) return;
        if(type == PacketType.PLAYERSHOOT.getIndex()) return;
        if(type == PacketType.SERVERPLAYERSHOOT.getIndex()) return;
        if(type == PacketType.USEPORTAL.getIndex()) return;
        if(type == PacketType.RECONNECT.getIndex()) return;
        if(type == PacketType.HELLO.getIndex()) return;
        if(type == PacketType.MAPINFO.getIndex()) return;
        if(type == PacketType.LOAD.getIndex()) return;
        if(type == PacketType.CREATE_SUCCESS.getIndex()) return;
        if(type == PacketType.EXALTATION_BONUS_CHANGED.getIndex()) return;
        if(type == PacketType.SHOOT_ACK_COUNTER.getIndex()) return;
        if(type == PacketType.NOTIFICATION.getIndex()) return;
        if(type == PacketType.FORGE_UNLOCKED_BLUEPRINTS.getIndex()) return;
        if(type == PacketType.VAULT_UPDATE.getIndex()) return;
        if(type == PacketType.GOTO.getIndex()) return;
        if(type == PacketType.GOTOACK.getIndex()) return;
        if(type == PacketType.PLAYSOUND.getIndex()) return;
        if(type == PacketType.INVRESULT.getIndex()) return;
        if(type == PacketType.USEITEM.getIndex()) return;

//        System.out.println(PacketType.byOrdinal(type));
//        System.out.println(packetType);
    }

    /**
     * Helper for debugging packets
     */
    private void debugPackets(int type, ByteBuffer data) {
        Packet packetType = PacketType.getPacket(type).factory();
        try {
            Util.print(PacketType.byOrdinal(type) + "");
            data.position(5);
            BufferReader pDebug = new BufferReader(data);
            pDebug.printError(packetType);
            packetType.deserialize(pDebug);
        } catch (Exception e) {
            Util.print(Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"));
        }
    }

    @Override
    public void resetIncoming() {
        incomingPacketConstructor.reset();
    }

    @Override
    public void resetOutgoing() {
        outgoingPacketConstructor.reset();
    }
}
