package experimental.input;

//import com.github.kwhat.jnativehook.GlobalScreen;
//import com.github.kwhat.jnativehook.NativeHookException;
//import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
//import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
//import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
//import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;
//import org.lwjgl.system.windows.User32;
//import potato.view.opengl.OpenGLPotato;
//import potato.model.DataModel;
//import util.FocusedWindow;
//
//import java.awt.*;
//
//public class MouseController implements NativeMouseWheelListener, NativeKeyListener {
//
//    DataModel model;
//    OpenGLPotato renderer;
//    ServerSynch server;
//
//    public MouseController(DataModel model, OpenGLPotato renderer, ServerSynch serverHTTP) {
//        this.model = model;
//        this.renderer = renderer;
//        this.server = serverHTTP;
//        try {
//            globalKeyMouseRegister();
////            System.out.println("clearconsole");
//        } catch (AWTException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void globalKeyMouseRegister() throws AWTException {
////        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
////        logger.setLevel(Level.OFF);
////        logger.setUseParentHandlers(false);
//        try {
//            GlobalScreen.registerNativeHook();
//        } catch (NativeHookException ex) {
//            System.err.println("There was a problem registering the native hook.");
//            System.err.println(ex.getMessage());
//
//            System.exit(1);
//        }
//
//        GlobalScreen.addNativeMouseWheelListener(this);
//        GlobalScreen.addNativeKeyListener(this);
//    }
//
//    @Override
//    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
////        System.out.println(FocusedWindow.getWindowFocus() + " mod:" + e.getModifiers() + " zoomed:" + e.getWheelRotation());
//        if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
//            model.editZoom(e.getWheelRotation());
//        }
////        if ((e.getModifiers() % 512) == 1) {
////            float f = OpenGLPotato.playerOffset[OpenGLPotato.zoom] + e.getWheelRotation() * 0.01f;
////            OpenGLPotato.playerOffset[OpenGLPotato.zoom] = f;
////            System.out.println("Offset:" + f);
////            model.refresh();
////        }
////        if ((e.getModifiers() % 512) == 2) {
////            float f = OpenGLPotato.scale[OpenGLPotato.zoom] + e.getWheelRotation() * 0.01f;
////            OpenGLPotato.scale[OpenGLPotato.zoom] = f;
////            System.out.println("Scale: " + f);
////            model.refresh();
////        }
////        if ((e.getModifiers() % 512) == 3) {
////            float f1 = OpenGLPotato.scale[OpenGLPotato.zoom] + e.getWheelRotation() * 0.01f;
////            float f2 = OpenGLPotato.playerOffset[OpenGLPotato.zoom] + e.getWheelRotation() * 2;
////            OpenGLPotato.scale[OpenGLPotato.zoom] = f1;
////            OpenGLPotato.playerOffset[OpenGLPotato.zoom] = f2;
////            System.out.println("S:" + f1 + " O:" + f2);
////            model.refresh();
////        }
//    }
//
//    @Override
//    public void nativeKeyPressed(NativeKeyEvent e) {
////        System.out.println(FocusedWindow.getWindowFocus() + " mod:" + e.getModifiers() + " " + e.getRawCode() + " " + e.getKeyCode());
////        if (e.getKeyCode() == NativeKeyEvent.VC_9) {
////            if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
////                renderer.hideTaskBarIcon();
////            }
////        } else if (e.getKeyCode() == NativeKeyEvent.VC_0) {
////            if (FocusedWindow.getWindowFocus().equals("RotMG Exalt.exe")) {
////                System.out.println("test");
////                renderer.showTaskBarIcon();
////            }
////        }
//    }
//
//    public void dispose() {
//        try {
//            GlobalScreen.unregisterNativeHook();
//        } catch (NativeHookException e) {
//            e.printStackTrace();
//        }
//    }
//}
