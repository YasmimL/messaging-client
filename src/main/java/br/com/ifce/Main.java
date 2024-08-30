package br.com.ifce;

import br.com.ifce.model.Message;
import br.com.ifce.model.enums.MessageType;
import br.com.ifce.network.socket.SocketClient;
import br.com.ifce.view.MainView;
import com.github.javafaker.Faker;

import javax.swing.*;

import static java.lang.Runtime.getRuntime;

public class Main {
    public static void main(String[] args) {
        final var socketClient = SocketClient.getInstance();
        socketClient.start();

        final var username = generateUsername();
        socketClient.send(new Message<>(MessageType.SUBSCRIBE, username));

        getRuntime().addShutdownHook(new Thread(() -> SocketClient.getInstance().close()));

        SwingUtilities.invokeLater(() -> {
            try {
                var view = new MainView(username);
                view.show();
                socketClient.setListener(view);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String generateUsername() {
        Faker faker = new Faker();
        return faker.superhero().prefix() + faker.name().firstName() + faker.address().buildingNumber();
    }
}