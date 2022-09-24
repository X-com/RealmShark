package potato;

import packets.PacketType;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import potato.control.PacketController;
import potato.model.DataModel;
import potato.view.GUIBase;
import tomato.Tomato;
import util.Util;

import java.net.URL;

// TODO: add tp cooldown
public class Potato extends Thread {
    static public URL imagePath = Tomato.class.getResource("/icon/potatoIcon.png");
    DataModel dataModel;
    PacketController controller;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        new Potato().run();
    }

    public void run() {
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
