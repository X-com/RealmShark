package potato.view;

import static potato.view.Win.NOTIFYICONDATA.*;
import static potato.view.Win.Shell32.*;
import static potato.view.Win.User32.*;
import static potato.view.Win.User32_64.*;

import java.util.concurrent.CyclicBarrier;

import potato.view.Win.MSG;
import potato.view.Win.NOTIFYICONDATA;
import potato.view.Win.POINT;
import potato.view.Win.Parameter;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public class Tray {
    final NOTIFYICONDATA windowNotifyIconData = new NOTIFYICONDATA();
    final NOTIFYICONDATA balloonNotifyIconData = new NOTIFYICONDATA();
    final POINT mousePosition = new POINT();
    Pointer hwnd;
    StdCallCallback wndProc;
    private String tooltip = "";

    public Tray(long window) {
        hwnd = new Pointer(window);
        final CyclicBarrier barrier = new CyclicBarrier(2);

        new Thread("Tray") {
            public void run () {
                hwnd = CreateWindowEx(0, new WString("STATIC"), new WString("Potato"), 0, 0, 0, 0, 0, 0, 0, 0,
                        0);
                if (hwnd == null) {
                    System.out.println("Unable to create tray window.");
                    System.exit(0);
                }

                final int wmTaskbarCreated = RegisterWindowMessage(new WString("TaskbarCreated"));
                final int wmTrayIcon = WM_USER + 1;

                String iconPath;
                try {
                    iconPath = "D:/Programmering/GitKraken/RealmShark/src/main/resources/icon/potatoIcon.png";
                } catch (Exception e) {
                    System.out.println("Unable to read icon." + e);
                    System.exit(0);
                    return;
                }
                windowNotifyIconData.hWnd = hwnd;
                windowNotifyIconData.uID = 1763;
                windowNotifyIconData.uFlags = NIF_ICON | NIF_MESSAGE;
                windowNotifyIconData.uCallbackMessage = wmTrayIcon;
                windowNotifyIconData.hIcon = LoadImage(null, new WString(iconPath), IMAGE_ICON, 0, 0, LR_LOADFROMFILE);
                windowNotifyIconData.setTooltip("Potato");
                Shell_NotifyIcon(NIM_ADD, windowNotifyIconData);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> Shell_NotifyIcon(NIM_DELETE, windowNotifyIconData)));

                wndProc = new StdCallCallback() {
                    public int callback (Pointer hwnd, int message, Parameter wParameter, Parameter lParameter) {
                        if (message == wmTrayIcon) {
                            int lParam = lParameter.intValue();
                            switch (lParam) {
                                case WM_LBUTTONDOWN:
                                    if (GetCursorPos(mousePosition)) mouseDown(mousePosition, 0);
                                    break;
                                case WM_LBUTTONUP:
                                    if (GetCursorPos(mousePosition)) mouseUp(mousePosition, 0);
                                    break;
                                case WM_RBUTTONDOWN:
                                    if (GetCursorPos(mousePosition)) mouseDown(mousePosition, 1);
                                    break;
                                case WM_RBUTTONUP:
                                    if (GetCursorPos(mousePosition)) mouseUp(mousePosition, 1);
                                    break;
                            }
                        } else if (message == wmTaskbarCreated) {
                            // Add icon again if explorer crashed.
                            Shell_NotifyIcon(NIM_ADD, windowNotifyIconData);
                        } else if (message == WM_CLOSE) {
                            System.out.println("click");
                        }
                        return DefWindowProc(hwnd, message, wParameter, lParameter);
                    }
                };
                if (Win.is64Bit)
                    SetWindowLongPtr(hwnd, GWL_WNDPROC, wndProc);
                else
                    SetWindowLong(hwnd, GWL_WNDPROC, wndProc);

                try {
                    barrier.await();
                } catch (Exception ignored) {
                }

                MSG msg = new MSG();
                while (GetMessage(msg, null, 0, 0)) {
                    TranslateMessage(msg);
                    DispatchMessage(msg);
                }
            }
        }.start();

        try {
            barrier.await();
        } catch (Exception ignored) {
        }
    }

    protected void mouseDown (POINT position, int button) {
        System.out.println("click d " + button);
    }

    protected void mouseUp (POINT position, int button) {
        System.out.println("click u " + button);
    }

    public synchronized void updateTooltip (String text) {
        if (tooltip.equals(text)) return;
        tooltip = text;
        windowNotifyIconData.setTooltip(text);
        Shell_NotifyIcon(NIM_MODIFY, windowNotifyIconData);
    }

    public synchronized void balloon (String title, String message, int millis) {
        balloonNotifyIconData.hWnd = this.windowNotifyIconData.hWnd;
        balloonNotifyIconData.uID = this.windowNotifyIconData.uID;
        balloonNotifyIconData.setBalloon(title, message, millis, NIIF_NONE);
        Shell_NotifyIcon(NIM_MODIFY, balloonNotifyIconData);
    }
}