package br.com.ifce.network.rmi;

import br.com.ifce.model.ChatMessage;

import java.rmi.Remote;
import java.util.List;

public interface OfflineMessagingService extends Remote {

    void createQueue(String queueName);

    void send(ChatMessage message);

    List<ChatMessage> readAll(String username);
}
