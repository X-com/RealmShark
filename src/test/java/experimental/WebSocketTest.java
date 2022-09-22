package experimental;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketTest {

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
//        URI url = new URI("ws://217.27.177.69:8080");
        URI url = new URI("ws://localhost:8080");
        System.out.println("start");
        WebSocketClient client = new WebSocketClient(url) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("Hello, it is me. Mario :)");
                System.out.println("opened connection");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("received: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        client.connect();
        System.out.println("end");
    }
}
