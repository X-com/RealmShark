package potato;

import packets.PacketType;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import potato.control.PacketController;
import potato.model.Config;
import potato.model.DataModel;
import potato.view.GUIBase;
import util.Util;

import java.net.URL;

// TODO: add tp cooldown
public class Potato {
    static public URL imagePath = Potato.class.getResource("/icon/potatoIcon.png");
    DataModel dataModel;
    PacketController controller;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        new Potato().run();
    }

    public void run() {
        Config.load();
        dataModel = new DataModel();
        new GUIBase(dataModel);
        controller = new PacketController(dataModel);

        Register.INSTANCE.register(PacketType.MAPINFO, controller::packets);
        Register.INSTANCE.register(PacketType.UPDATE, controller::packets);
        Register.INSTANCE.register(PacketType.NEWTICK, controller::packets);
        Register.INSTANCE.register(PacketType.REALM_HERO_LEFT_MSG, controller::packets);
        Register.INSTANCE.register(PacketType.TEXT, controller::packets);
        Register.INSTANCE.register(PacketType.QUESTOBJID, controller::packets);
        Register.INSTANCE.register(PacketType.IP_ADDRESS, controller::packets);
        Register.INSTANCE.register(PacketType.CREATE_SUCCESS, controller::packets);

        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.start();
    }
}
