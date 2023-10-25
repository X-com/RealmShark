package potato.control;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WebSocket extends WebSocketClient {
    boolean isConnected = false;

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        WebSocket ws = new WebSocket("ws://localhost:8080");
        ws.connectBlocking(10, TimeUnit.SECONDS);
        ws.sendString("hello");
        ws.close();
    }

    public WebSocket(String uri) throws URISyntaxException {
        super(new URI(uri));
    }

    public void sendString(String s) {
        if (!isConnected) return;
        send(s);
    }

    public void sendBytes(byte[] b) {
        if (!isConnected) return;
        send(b);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection " + handshakedata);
        isConnected = true;
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        isConnected = false;
        closed();
    }

    public void closed() {
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
