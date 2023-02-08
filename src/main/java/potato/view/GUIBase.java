package potato.view;

import potato.Potato;
import potato.control.ScreenLocatorController;
import potato.model.Config;
import potato.model.DataModel;
import potato.view.opengl.OpenGLPotato;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GUIBase {

    private final ScreenLocatorController aligner;

    public GUIBase(DataModel dataModel) {
        this.aligner = dataModel.getAligner();
        makeTrayIcon();
    }

    private void makeTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported");
            return;
        }
        //get the systemTray of the system
        SystemTray systemTray = SystemTray.getSystemTray();

        //get default toolkit
        //Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage image = null;
        try {
            image = ImageIO.read(Potato.imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PopupMenu trayPopupMenu = new PopupMenu();
        System.out.println("Config.instance.showMap " + Config.instance.showMap);
        MenuItem hideMap = new MenuItem(Config.instance.showMap ? "Hide Map" : "Show Map");
        MenuItem hideHeroes = new MenuItem(Config.instance.showHeroes ? "Hide Heroes" : "Show Heroes");
        MenuItem hideCoords = new MenuItem(Config.instance.showInfo ? "Hide Info" : "Show Info");

        OpenGLPotato.showMap(Config.instance.showMap);
        OpenGLPotato.showHeroes(Config.instance.showHeroes);
        OpenGLPotato.showInfo(Config.instance.showInfo);

        hideMap.addActionListener(e -> {
            if (Config.instance.showMap) {
                OpenGLPotato.showMap(false);
                hideMap.setLabel("Show Map");
                Config.instance.showMap = false;
                Config.save();
            } else {
                OpenGLPotato.showMap(true);
                hideMap.setLabel("Hide Map");
                Config.instance.showMap = true;
                Config.save();
            }
        });
        hideHeroes.addActionListener(e -> {
            if (Config.instance.showHeroes) {
                OpenGLPotato.showHeroes(false);
                hideHeroes.setLabel("Show Heroes");
                Config.instance.showHeroes = false;
                Config.save();
            } else {
                OpenGLPotato.showHeroes(true);
                hideHeroes.setLabel("Hide Heroes");
                Config.instance.showHeroes = true;
                Config.save();
            }
        });
        hideCoords.addActionListener(e -> {
            if (Config.instance.showInfo) {
                OpenGLPotato.showInfo(false);
                hideCoords.setLabel("Show Info");
                Config.instance.showInfo = false;
                Config.save();
            } else {
                OpenGLPotato.showInfo(true);
                hideCoords.setLabel("Hide Info");
                Config.instance.showInfo = true;
                Config.save();
            }
        });
        trayPopupMenu.add(hideMap);
        trayPopupMenu.add(hideHeroes);
        trayPopupMenu.add(hideCoords);

        trayPopupMenu.addSeparator();

        MenuItem align = new MenuItem("Align Minimap");
        align.addActionListener(e -> aligner.calcMapSizeLoc2());
        trayPopupMenu.add(align);

        MenuItem options = new MenuItem("Options");
        options.addActionListener(e -> optionsMenu());
        trayPopupMenu.add(options);

        MenuItem close = new MenuItem("Close");
        close.addActionListener(e -> System.exit(0));
        trayPopupMenu.add(close);

        TrayIcon trayIcon = new TrayIcon(image, "Potato", trayPopupMenu);
        trayIcon.setImageAutoSize(true);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException awtException) {
            awtException.printStackTrace();
        }
    }

    private void optionsMenu() {
        OptionsMenu.showOptions();
    }
}
