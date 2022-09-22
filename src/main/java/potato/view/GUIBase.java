package potato.view;

import potato.Potato;
import potato.control.ScreenLocatorController;

import javax.swing.*;
import java.awt.*;

public class GUIBase {

    private JFrame menuFrame;
    private static Image icon = Toolkit.getDefaultToolkit().getImage(Potato.imagePath);

    public GUIBase() {
        makeTrayIcon();
    }

    private void smallWindow() {
        menuFrame = new JFrame("Potato") {
            @Override
            public void dispose() {
                super.dispose();
                System.exit(0);
            }
        };
        menuFrame.setIconImage(icon);
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
        Image image = Toolkit.getDefaultToolkit().getImage(Potato.imagePath);

        PopupMenu trayPopupMenu = new PopupMenu();
        MenuItem hideMap = new MenuItem("Hide Map");
        MenuItem hideHeroes = new MenuItem("Hide Heroes");
        MenuItem hideCoords = new MenuItem("Hide Coords");
        hideMap.addActionListener(e -> {
            if (hideMap.getLabel().startsWith("Hide")) {
                RenderViewer.showMap(false);
                hideMap.setLabel("Show Map");
            } else {
                RenderViewer.showMap(true);
                hideMap.setLabel("Hide Map");
            }
        });
        hideHeroes.addActionListener(e -> {
            if (hideHeroes.getLabel().startsWith("Hide")) {
                RenderViewer.showHeroes(false);
                hideHeroes.setLabel("Show Heroes");
            } else {
                RenderViewer.showHeroes(true);
                hideHeroes.setLabel("Hide Heroes");
            }
        });
        hideCoords.addActionListener(e -> {
            if (hideCoords.getLabel().startsWith("Hide")) {
                RenderViewer.showCoords(false);
                hideCoords.setLabel("Show Coords");
            } else {
                RenderViewer.showCoords(true);
                hideCoords.setLabel("Hide Coords");
            }
        });
        trayPopupMenu.add(hideMap);
        trayPopupMenu.add(hideHeroes);
        trayPopupMenu.add(hideCoords);

        trayPopupMenu.addSeparator();

        MenuItem windowScaling = new MenuItem("Enable Scaling");
        windowScaling.addActionListener(e -> {
            if (windowScaling.getLabel().startsWith("Enable")) {
                ScreenLocatorController.setScaling(true);
                windowScaling.setLabel("Disable Scaling");
            } else {
                ScreenLocatorController.setScaling(false);
                windowScaling.setLabel("Enable Scaling");
            }
        });
        trayPopupMenu.add(windowScaling);

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
