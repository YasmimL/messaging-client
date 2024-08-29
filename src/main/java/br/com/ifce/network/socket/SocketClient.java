package br.com.ifce.network.socket;

import br.com.ifce.model.Message;
import br.com.ifce.model.enums.MessageType;
import br.com.ifce.network.MessageListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {

    private static final String SERVER_ADDRESS = "localhost";

    private static final int SERVER_PORT = 1234;

    private static final SocketClient INSTANCE = new SocketClient();

    private Socket socket;

    private Boolean interrupted = false;

    private MessageListener listener;

    public static SocketClient getInstance() {
        return INSTANCE;
    }

    public void start() {
        try {
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
//            this.send(new Message<>(MessageType.SUBSCRIBE, null));
            this.listenForServerMessages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listenForServerMessages() {
        new Thread(() -> {
            while (!this.interrupted) {
                try {
                    var inputStream = new ObjectInputStream(this.socket.getInputStream());
                    this.listener.onMessage((Message<?>) inputStream.readObject());
                } catch (Exception e) {
//                    this.close();
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void send(MessageType messageType) {
        this.send(new Message<>(messageType, null));
    }

    public void send(Message<?> message) {
        try {
            var outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            outputStream.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
}
