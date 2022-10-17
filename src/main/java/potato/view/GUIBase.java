package potato.view;

import potato.Potato;
import potato.model.DataModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GUIBase {

    public static final Image potatoIcon = Toolkit.getDefaultToolkit().getImage(Potato.imagePath);
    private final DataModel dataModel;

    public GUIBase(DataModel dataModel) {
        this.dataModel = dataModel;
        makeTrayIcon();
    }

    private void smallWindow() {
        JFrame menuFrame = new JFrame("Potato") {
            @Override
            public void dispose() {
                super.dispose();
                dataModel.dispose();
                System.exit(0);
            }
        };
        menuFrame.setIconImage(potatoIcon);
        menuFrame.setSize(100, 100);
        menuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        menuFrame.setVisible(true);
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
        MenuItem hideMap = new MenuItem("Hide Map");
        MenuItem hideHeroes = new MenuItem("Hide Heroes");
        MenuItem hideCoords = new MenuItem("Hide Info");
        hideMap.addActionListener(e -> {
            if (hideMap.getLabel().startsWith("Hide")) {
                OpenGLPotato.showMap(false);
                hideMap.setLabel("Show Map");
            } else {
                OpenGLPotato.showMap(true);
                hideMap.setLabel("Hide Map");
            }
        });
        hideHeroes.addActionListener(e -> {
            if (hideHeroes.getLabel().startsWith("Hide")) {
                OpenGLPotato.showHeroes(false);
                hideHeroes.setLabel("Show Heroes");
            } else {
                OpenGLPotato.showHeroes(true);
                hideHeroes.setLabel("Hide Heroes");
            }
        });
        hideCoords.addActionListener(e -> {
            if (hideCoords.getLabel().startsWith("Hide")) {
                OpenGLPotato.showInfo(false);
                hideCoords.setLabel("Show Info");
            } else {
                OpenGLPotato.showInfo(true);
                hideCoords.setLabel("Hide Info");
            }
        });
        trayPopupMenu.add(hideMap);
        trayPopupMenu.add(hideHeroes);
        trayPopupMenu.add(hideCoords);

        trayPopupMenu.addSeparator();

//        MenuItem windowScaling = new MenuItem("Enable Scaling");
//        windowScaling.addActionListener(e -> {
//            if (windowScaling.getLabel().startsWith("Enable")) {
//                ScreenLocatorController.setScaling(true);
//                windowScaling.setLabel("Disable Scaling");
//            } else {
//                ScreenLocatorController.setScaling(false);
//                windowScaling.setLabel("Enable Scaling");
//            }
//        });
//        trayPopupMenu.add(windowScaling);

        MenuItem options = new MenuItem("Options");
        options.addActionListener(e -> JOptionPane.showMessageDialog(null, "Options not added yet."));
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
}
