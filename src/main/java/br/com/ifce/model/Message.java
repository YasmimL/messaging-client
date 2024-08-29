package br.com.ifce.model;

import br.com.ifce.model.enums.MessageType;

import java.io.Serializable;

public class Message<T> implements Serializable {

    private MessageType type;

    private T payload;

    public Message(MessageType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }
}
