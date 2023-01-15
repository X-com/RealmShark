package potato.control;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import potato.model.DataModel;
import potato.view.OpenGLPotato;
import util.FocusedWindow;

public class MouseController {

    DataModel model;
    OpenGLPotato renderer;
    ServerSynch server;
    GlobalMouseHook mouseHook;
    GlobalKeyboardHook keyboardHook;

    public MouseController(DataModel model, OpenGLPotato renderer, ServerSynch serverHTTP) {
        this.model = model;
        this.renderer = renderer;
        this.server = serverHTTP;
        mouseHook = new GlobalMouseHook();
        keyboardHook = new GlobalKeyboardHook(true);
        registerListeners();
    }

    public void registerListeners() {
        mouseHook.addMouseListener(new GlobalMouseAdapter() {

//            @Override
//            public void mousePressed(GlobalMouseEvent event) {
//                System.out.println(event);
//            }
//
//            @Override
//            public void mouseReleased(GlobalMouseEvent event) {
//                System.out.println(event);
//            }
//
//            @Override
//            public void mouseMoved(GlobalMouseEvent event) {
//                System.out.println(event);
//            }

            @Override
            public void mouseWheel(GlobalMouseEvent event) {
//                System.out.println(event);
                mouseWheelMoved(event);
            }
        });

        keyboardHook.addKeyListener(new GlobalKeyAdapter() {

            @Override
            public void keyPressed(GlobalKeyEvent event) {
//                System.out.println(event);
                keyboardPressed(event);
            }

//            @Override
//            public void keyReleased(GlobalKeyEvent event) {
//                System.out.println(event);
//            }
        });
    }

    public void mouseWheelMoved(GlobalMouseEvent event) {
        if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
            int i = event.getDelta() / GlobalMouseEvent.WHEEL_DELTA * -1;
            model.editZoom(i);
        }
    }

    public void keyboardPressed(GlobalKeyEvent event) {
    }

    public void dispose() {
        mouseHook.shutdownHook();
    }
}
