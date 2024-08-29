package br.com.ifce.network;

import br.com.ifce.model.Message;

public interface MessageListener {
    void onMessage(Message<?> message);
}
