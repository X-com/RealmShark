package potato.control;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import potato.model.Config;
import potato.model.DataModel;
import potato.view.OptionsMenu;
import potato.view.opengl.OpenGLPotato;
import potato.view.opengl.WindowGLFW;

import java.util.EventObject;

public class InputController {

    private DataModel model;
    private OpenGLPotato renderer;
    private ServerSynch server;
    private GlobalMouseHook mouseHook;
    private GlobalKeyboardHook keyboardHook;
    private GlobalKeyAdapter globKey;
    private int keyMod = 0;

    public InputController(DataModel model, OpenGLPotato renderer, ServerSynch serverHTTP) {
        this.model = model;
        this.renderer = renderer;
        this.server = serverHTTP;
        mouseHook = new GlobalMouseHook();
        keyboardHook = new GlobalKeyboardHook(true);
        registerListeners();
    }

    public void registerListeners() {
        mouseHook.addMouseListener(new GlobalMouseAdapter() {
            @Override
            public void mousePressed(GlobalMouseEvent event) {
                keyMousePressed(event);
                OptionsMenu.keyChanges(event, keyMod);
            }

            @Override
            public void mouseWheel(GlobalMouseEvent event) {
                keyMousePressed(event);
                OptionsMenu.keyChanges(event, keyMod);
            }
        });

        globKey = new GlobalKeyAdapter() {
            @Override
            public void keyReleased(GlobalKeyEvent event) {
                keyMod(event);
                OptionsMenu.keyChanges(event, keyMod);
            }

            @Override
            public void keyPressed(GlobalKeyEvent event) {
                keyMod(event);
                keyMousePressed(event);
                OptionsMenu.keyChanges(event, keyMod);
            }
        };
        keyboardHook.addKeyListener(globKey);
    }

    private void keyMousePressed(EventObject event) {
        if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
            int key = 0;
            if (event instanceof GlobalMouseEvent) {
                GlobalMouseEvent e = (GlobalMouseEvent) event;
                if (e.getTransitionState() == e.TS_DOWN) {
                    key = e.getButton();
                } else if (e.getTransitionState() == e.TS_WHEEL) {
                    key = e.getDelta() == GlobalMouseEvent.WHEEL_DELTA ? 200 : 201;
                }
            } else if (event instanceof GlobalKeyEvent) {
                GlobalKeyEvent e = (GlobalKeyEvent) event;
                key = e.getVirtualKeyCode();
            }

            key |= keyMod;
            if (key == Config.instance.keyValues[0]) {
                model.editZoom(true);
            } else if (key == Config.instance.keyValues[1]) {
                model.editZoom(false);
            } else if (key == Config.instance.keyValues[2]) {
                OpenGLPotato.toggleShowMap();
            } else if (key == Config.instance.keyValues[3]) {
                OpenGLPotato.toggleShowHeroes();
            } else if (key == Config.instance.keyValues[4]) {
                OpenGLPotato.toggleShowInfo();
            } else if (key == Config.instance.keyValues[5]) {
                renderer.toggleShowAll();
            } else if (key == Config.instance.keyValues[6]) {
                Config.instance.saveMapInfo = !Config.instance.saveMapInfo;
                Config.save();
            }
        }
    }

//    private void mouseWheelMoved(GlobalMouseEvent event) {
//        if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
//            int i = event.getDelta() / GlobalMouseEvent.WHEEL_DELTA * -1;
//            model.editZoom(i);
//        }
//    }

//    private void keyboardPressed(GlobalKeyEvent event) {
//        if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
//            int virtualKeyCode = event.getVirtualKeyCode();
//            if (virtualKeyCode == event.VK_A && event.isShiftPressed()) {
//                OpenGLPotato.toggleShowMap();
//            }
//            if (virtualKeyCode == event.VK_S && event.isShiftPressed()) {
//                OpenGLPotato.toggleShowHeroes();
//            }
//            if (virtualKeyCode == event.VK_D && event.isShiftPressed()) {
//                OpenGLPotato.toggleShowInfo();
//            }
//        }
//        keyMod(event);
//    }

    private void keyMod(GlobalKeyEvent event) {
        int m = 0;
        m |= event.isShiftPressed() ? 0x10000000 : 0;
        m |= event.isControlPressed() ? 0x20000000 : 0;
        m |= event.isMenuPressed() ? 0x40000000 : 0;

        keyMod = m;
    }

    public void dispose() {
        mouseHook.shutdownHook();
    }
}
