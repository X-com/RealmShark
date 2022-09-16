package potato.control;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;
import potato.model.DataModel;
import potato.view.RenderViewer;
import util.FocusedWindow;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MouseKeyController implements NativeMouseWheelListener, NativeMouseListener, NativeKeyListener {

    DataModel model;
    RenderViewer renderer;
    ServerHTTP serverHTTP;

    public MouseKeyController(DataModel model, RenderViewer renderer, ServerHTTP serverHTTP) {
        this.model = model;
        this.renderer = renderer;
        this.serverHTTP = serverHTTP;
        try {
            globalKeyMouseRegister();
            System.out.println("clearconsole");
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
//        System.out.println("e.getRawCode(): " + e.getRawCode() + " mod: " + e.getModifiers());
        if ((e.getModifiers() % 512) == 3) {
            if (e.getRawCode() == 66) {
                model.uploadMap();
            } else if (e.getRawCode() == 77) {
                renderer.toggleMap();
            } else if (e.getRawCode() == 78) {
                renderer.toggleDots();
            }
        }
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
//        System.out.println("Click: " + e.getButton() + " mod: " + e.getModifiers() + " " + (e.getModifiers() % 512));
        if ((e.getModifiers() % 512) == 1 && e.getButton() == 1) { // mark active
//            HeroLocations h = model.findClosestHero();
//            model.markActive(h);
        } else if ((e.getModifiers() % 512) == 1 && e.getButton() == 2) { // mark dead
//            HeroLocations h = model.findClosestHero();
//            model.markDead(h);
        } else if ((e.getModifiers() % 512) == 3 && e.getButton() == 3) {
            model.synch();
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
//        System.out.println("Mosue Wheel Moved: " + e.getWheelRotation() + " modifier: " + e.getModifiers());
        if ((e.getModifiers() % 512) == 0) {
//            GetForegroundWindow();
            if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
                model.editZoom(e.getWheelRotation());
            }
//        } else if ((e.getModifiers() % 512) == 32) {
//            RenderViewer.imageSize[RenderViewer.zoom] += e.getWheelRotation();
////            RenderViewer.imageM[RenderViewer.zoom] -= (e.getWheelRotation() * 0.0005);
//            System.out.println("imageSize: " + RenderViewer.imageSize[RenderViewer.zoom]);
//
////            RenderViewer.imageOffsetX = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerX + RenderViewer.imageK[RenderViewer.zoom]);
////            RenderViewer.imageOffsetY = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerY + RenderViewer.imageK[RenderViewer.zoom]);
//        } else if ((e.getModifiers() % 512) == 2) {
//            RenderViewer.imageM[RenderViewer.zoom] += (e.getWheelRotation() * 0.001);
//            System.out.println("imageM: " + RenderViewer.imageM[RenderViewer.zoom]);
//
//            RenderViewer.imageOffsetX = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerX + RenderViewer.imageK[RenderViewer.zoom]);
//            RenderViewer.imageOffsetY = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerY + RenderViewer.imageK[RenderViewer.zoom]);
//        } else if ((e.getModifiers() % 512) == 1) {
//            RenderViewer.imageK[RenderViewer.zoom] += e.getWheelRotation();
//            System.out.println("imageK: " + RenderViewer.imageK[RenderViewer.zoom]);
//
//            RenderViewer.imageOffsetX = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerX + RenderViewer.imageK[RenderViewer.zoom]);
//            RenderViewer.imageOffsetY = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerY + RenderViewer.imageK[RenderViewer.zoom]);
//        } else if ((e.getModifiers() % 512) == 48) {
//            RenderViewer.imageK[RenderViewer.zoom] += e.getWheelRotation();
//            RenderViewer.imageM[RenderViewer.zoom] -= (e.getWheelRotation() * 0.001);
//            System.out.println("imageK: " + RenderViewer.imageK[RenderViewer.zoom]);
//            System.out.println("imageM: " + RenderViewer.imageM[RenderViewer.zoom]);
//
//            RenderViewer.imageOffsetX = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerX + RenderViewer.imageK[RenderViewer.zoom]);
//            RenderViewer.imageOffsetY = (int) (RenderViewer.imageM[RenderViewer.zoom] * RenderViewer.playerY + RenderViewer.imageK[RenderViewer.zoom]);
//        } else if ((e.getModifiers() % 512) == 16) {
        } else if (e.getModifiers() == 3) {
            model.editMapIndex(e.getWheelRotation());
            serverHTTP.stopSynch();
        }
    }

    private void globalKeyMouseRegister() throws AWTException {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            System.exit(1);
        }

        GlobalScreen.addNativeMouseWheelListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeKeyListener(this);
    }

}
