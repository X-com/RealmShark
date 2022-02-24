package packets.packetcapture.register;

import packets.Packet;
import packets.PacketType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The registry class is used to subscribe to either all or specific packets. If registered packets
 * are received the emit method will send an update and trigger the lambda used.
 */
public class Register {
    public static final Register INSTANCE = new Register();
    private final HashMap<Class<? extends Packet>, ArrayList<IPacketListener<Packet>>> packetListeners = new HashMap<>();

    /**
     * Emitter for sending packets to any subscriber which matches the packets the subscriber have subbed too.
     *
     * @param packet The packet being received and emitted.
     */
    public void emit(Packet packet) {
        if (packetListeners.containsKey(packet.getClass())) {
            for (IPacketListener<Packet> processor : packetListeners.get(packet.getClass()))
                processor.process(packet);
        }

        if (packetListeners.containsKey(Packet.class)) {
            for (IPacketListener<Packet> processor : packetListeners.get(Packet.class))
                processor.process(packet);
        }
    }

    /**
     * Register method to subscribe to packets that are being received from the network tap.
     *
     * @param type      The type of class wanting to be subscribed too.
     * @param processor The lambda needed to trigger what event should happen if packet is received.
     * @param <T>       Class type.
     */
    public <T extends Class<? extends Packet>> void register(PacketType type, IPacketListener<Packet> processor) {
        packetListeners.computeIfAbsent(type.getPacketClass(), (a) -> new ArrayList<>()).add(processor);
    }

    /**
     * Register method to subscribe to all packets that are being received from the network tap.
     *
     * @param processor The lambda needed to trigger what event should happen if packet is received.
     * @param <T>       Class type.
     */
    public <T extends Class<? extends Packet>> void registerAll(IPacketListener<Packet> processor) {
        packetListeners.computeIfAbsent(Packet.class, (a) -> new ArrayList<>()).add(processor);
    }
}
