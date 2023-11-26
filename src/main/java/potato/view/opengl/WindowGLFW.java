package potato.view.opengl;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.lwjgl.opengl.GL;
import potato.model.Config;

import static com.sun.jna.platform.win32.WinUser.GWL_EXSTYLE;
import static com.sun.jna.platform.win32.WinUser.WS_VISIBLE;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.windows.User32.WS_EX_APPWINDOW;
import static org.lwjgl.system.windows.User32.WS_EX_TOOLWINDOW;

public class WindowGLFW {

    private long window;
    private static boolean viewChanged;
    private static boolean userShowAll = true;

    public WindowGLFW() {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        window = glfwCreateWindow(Config.instance.mapWidth, Config.instance.mapHeight, "Potato", NULL, NULL);
        glfwSetWindowPos(window, Config.instance.mapTopLeftX, Config.instance.mapTopLeftY);
        WinDef.HWND hwnd = new WinDef.HWND(new Pointer(glfwGetWin32Window(window)));

        // hide Task Bar Icon
        int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        style &= ~(WS_VISIBLE);    // this works - window become invisible

        style |= WS_EX_TOOLWINDOW;   // flags don't work - windows remains in taskbar
        style &= ~(WS_EX_APPWINDOW);

        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, style);
        // -----------

//        ratio = (float) Config.instance.mapWidth / Config.instance.mapHeight;
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to open GLFW window. If you have an Intel GPU, they are not 3.3 compatible. Try the 2.1 version of the tutorials.");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();
        glfwShowWindow(window);
        System.out.println("Using GL Version: " + glGetString(GL_VERSION));

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void swapBuffer() {
        glfwSwapBuffers(window); // Update Window
        glfwPollEvents(); // Key Mouse Input
    }

    public boolean shouldWindowClose() {
        return glfwWindowShouldClose(window);
    }

    public void show() {
        try {
            glfwShowWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        try {
            glfwHideWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleShowAll() {
        if (userShowAll) {
            hide();
            userShowAll = false;
        } else {
            show();
            userShowAll = true;
        }
    }

    public static void viewChanged() {
        viewChanged = true;
    }

    public void checkViewChange() {
        if (viewChanged) {
            glfwSetWindowPos(window, Config.instance.mapTopLeftX, Config.instance.mapTopLeftY);
            glfwSetWindowSize(window, Config.instance.mapWidth, Config.instance.mapHeight);
            glViewport(0, 0, Config.instance.mapWidth, Config.instance.mapHeight);
            viewChanged = false;
        }
    }
}
