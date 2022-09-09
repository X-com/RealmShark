package potato;

import packets.PacketType;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import potato.control.PacketController;
import potato.model.DataModel;
import util.Util;

// TODO: add tp cooldown
public class Potato extends Thread {
    DataModel dataModel;
    PacketController controller;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        new Potato().run();
    }

    public void run() {
        dataModel = new DataModel();
        controller = new PacketController(dataModel);

        Register.INSTANCE.register(PacketType.MAPINFO, controller::packets);
        Register.INSTANCE.register(PacketType.UPDATE, controller::packets);
        Register.INSTANCE.register(PacketType.NEWTICK, controller::packets);
        Register.INSTANCE.register(PacketType.REALM_HERO_LEFT_MSG, controller::packets);
        Register.INSTANCE.register(PacketType.TEXT, controller::packets);

        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.start();
    }
}
