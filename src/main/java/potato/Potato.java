package potato;

import packets.PacketType;
import packets.packetcapture.PacketProcessor;
import packets.packetcapture.register.Register;
import packets.packetcapture.sniff.assembly.TcpStreamErrorHandler;
import potato.control.PacketController;
import potato.model.Config;
import potato.model.DataModel;
import potato.view.GUIBase;
import util.Util;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

// TODO: add tp cooldown
public class Potato {
    static public URL imagePath = Potato.class.getResource("/icon/potatoIcon.png");
    DataModel dataModel;
    PacketController controller;

    public static void main(String[] args) {
        Util.setSaveLogs(true); // turns the logger to, save in to files.
        TcpStreamErrorHandler.INSTANCE.setErrorMessageHandler(Potato::errorMessage);
        try {
            new Potato().run();
        } catch (OutOfMemoryError e) {
            crashDialog();
        } catch (Exception e) {
            e.printStackTrace();
            Util.printLogs("Main crash:");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Util.printLogs(sw.toString());
        }
    }

    private static void errorMessage(String errorMsg, String errorDump) {
        Util.printLogs(errorDump);
    }

    private static void crashDialog() {
        JEditorPane ep = new JEditorPane("text/html", "<html>Out of memory crash.<br>Please uninstall 32-bit and install 64-bit Java.<br><a href=\\\\\\\"https://www.java.com/en/download/manual.jsp\\\\\\\">Download java 64-bit</a></html>");
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.java.com/en/download/manual.jsp"));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        JOptionPane.showMessageDialog(null, ep);
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
        Register.INSTANCE.register(PacketType.MOVE, controller::packets);

        PacketProcessor packetProcessor = new PacketProcessor();
        packetProcessor.start();
    }
}
